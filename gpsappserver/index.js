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

let coords = [[ 49.977376, 7.081916],
              [ 49.977498, 7.082554],
              [ 49.977127, 7.082847],
              [ 49.975834, 7.080333],
              [ 49.976496, 7.079528]]

var CheckPoint = mongoose.model("CheckPoint", {lat:Number, lon:Number, spot:Number})
var Route = mongoose.model("Route", {name:String, checkpoints:[]})
var Position = mongoose.model("Position", {id: String, lat:Number, lon: Number, time: Number}) // time in milliseconds
let route = new Route({name:"Robert Schuman Route", checkpoints:[]})


coords.forEach((coord,i)=>{
  let checkPoint = new CheckPoint({lat:coord[0], lon:coord[1],spot:(i+1)})
  route.checkpoints.push(checkPoint)
})

console.log(route)


function emitRank(Laptime,callback){

  d3.csv("file:///home/stefan/AndroidStudioProjects/gpsappserver/test.csv", function(data){
    let rank=1
    data.forEach(function(x){
      if ((x["Laptime"].replace(":","")-Laptime.replace(":",""))<0){
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
    socket.emit('route',route);
  })

  socket.on('finish', (msg) => {
    row=`${msg.times}`
    console.log(row)
    fs.appendFile(`test.csv`,
      row+'\n',()=>{})
    let LapTime= row.substr(row.length - 5)
    //schicke an server 2 die Lapdaten
    serverIo.emit("row",row)
    emitRank(LapTime,(erg)=>{
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
