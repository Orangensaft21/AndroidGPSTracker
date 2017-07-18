var io = require('socket.io')(3002)
var serverIo = require('socket.io')(3003)
var fs = require('fs')
var {user,pwd} = require('./config.json')
var mongoose = require('mongoose')
var d3 = require('d3')

//let conn = `mongodb://${user}:${pwd}@79.143.181.113:27017/hermdb`
//mongoose.connect(conn)
//var db = mongoose.connection
//db.on('error', console.error.bind(console, 'connection error:'))

let coordsRobSchum = [[ 49.977376, 7.081916],
              [ 49.977498, 7.082554],
              [ 49.977127, 7.082847],
              [ 49.975834, 7.080333],
              [ 49.976496, 7.079528]]

let coordsNorma = [[ 49.977376, 7.081916],
              [ 49.976496, 7.079528],
              [ 49.975093, 7.076811],
              [ 49.974331, 7.077028],
              [ 49.975093, 7.076811],
              [ 49.976496, 7.079528]]

let coordsSportplatz = [[49.957243, 7.104543],
                        [49.957997, 7.103231]]

var CheckPoint = mongoose.model("CheckPoint", {lat:Number, lon:Number, spot:Number})
var Route = mongoose.model("Route", {name:String, checkpoints:[]})
var Position = mongoose.model("Position", {id: String, lat:Number, lon: Number, time: Number}) // time in milliseconds

function createRoute(rname,coords,callback){
  let route = new Route({name:rname, checkpoints:[]})
  coords.forEach((coord,i)=>{
    let checkPoint = new CheckPoint({lat:coord[0], lon:coord[1],spot:(i+1)})
    route.checkpoints.push(checkPoint)
  })
  //console.log(route);
  callback(route);
}


function emitRank(Laptime,routeName,callback){
  //TODO Feste URL ändern
  let PATH = `file:///home/stefan/AndroidStudioProjects/gpsappserver/${routeName}-Rekorde.csv`
  d3.csv(PATH, function(data){
    let rank=1
    // TODO SIEHT SCHEISSE AUS
    console.log(Laptime)
    let Lap = Laptime.replace(":","")

    data.forEach((x) => {
      let test = x["Laptime"].split(":").join("")
      console.log (test + " " + Lap)
      console.log (parseFloat(test)+" "+parseFloat(Lap)+" ")
      if (parseFloat(test)-parseFloat(Lap)<0){
        rank+=1
      }
    })
    console.log(rank)
    callback(rank)
  });

}





io.on('connection', (socket) => {


  socket.on('test1', (msg) =>{
    /*console.log(`${msg.id} ${msg.location.lat} ${msg.location.lon} ${new Date().toISOString()}`)
    fs.appendFile(`${msg.id} ${new Date().toISOString().slice(0,10)}`,
      `${msg.id} ${msg.location.lat} ${msg.location.lon} ${new Date().toISOString().slice(11,-1)}\n`,()=>{})
    */
  })

  socket.on('reqcheck', (msg) => {
    //msg wird route beinhalten
    switch (msg) {
      case "RobSchumRoute":
        createRoute("Robert-Schuman-Route",coordsRobSchum,(createdRoute) =>{
          socket.emit('route',createdRoute);
        })

        break;
      case "NormaRoute":
        createRoute("Norma Route",coordsNorma,(createdRoute) =>{
          socket.emit('route',createdRoute);
        })
        break;
      case "Sportplatz":
        createRoute("Sportplatz Lap",coordsSportplatz,(createdRoute) =>{
          socket.emit('route',createdRoute);
        })
        break;
    }
  })

  socket.on('finish', (msg) => {
    row=`${msg.times}`
    console.log(row)
    fs.appendFile(`${msg.routeName}-Rekorde.csv`,
      row+'\n',()=>{})
    let LapTime= row.substr(row.length - 5)
    //schicke an server 2 die Lapdaten
    serverIo.emit("row",row)
    LapTime= row.substr(row.length - 8)
    emitRank(LapTime,msg.routeName,(erg)=>{
      console.log(erg)
      if (erg === 1){
        io.emit("zieldurchsage","New World Record")
      }
      socket.emit("zieldurchsage","You finished with Rank "+erg)
      socket.broadcast.emit("zieldurchsage", row.substr(0,row.indexOf(","))+ " finished with Rank "+erg)
    })
  });



  console.log('client connected')
  socket.on('disconnect', ()=>{
    //stream.end() //add listener for button
  })
})
