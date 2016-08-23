import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let defaultCommand = ''
let lastPMd = ''  // the last person private messaged

cli
  .delimiter(cli.chalk['yellow']('$>'))  //what is shown before you type

cli
  .mode('connect <username> [server] = localhost')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    server = connect({ host: args.server, port: 8080 }, () => {

      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      if(Message.fromJSON(buffer).command === 'echo'){
      this.log(cli.chalk.red(Message.fromJSON(buffer).toString()))
    } else if(Message.fromJSON(buffer).command === 'broadcast'){
    this.log(cli.chalk.magenta(Message.fromJSON(buffer).toString()))
  } else if(Message.fromJSON(buffer).command === '@'){
  this.log(cli.chalk.blue(Message.fromJSON(buffer).toString()))
} else {
this.log(cli.chalk.green(Message.fromJSON(buffer).toString()))
}
  })

    server.on('end', () => {
      cli.exec('exit')
      this.log("-----------------------------THIS IS WHAT GOES ON HERE------------------------------")
    })
  })
  .action(function (input, callback) {
    let entered = words(input, /\S*/g)
    let [ command, ...rest ] = entered
    let contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    }
    else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')

    } else if (command === 'broadcast' || command === 'say') {
      command = 'broadcast'
      server.write(new Message({ username, command, contents }).toJSON() + '\n')

    } else if (command.charAt(0) === '@') {



      server.write(new Message({ username, command, contents }).toJSON() + '\n')

    } else if (command === 'users') {
      server.write(new Message({ username, command }).toJSON() + '\n')
    } else {
      command = defaultCommand
      const [ ...rest ] = entered
      let contents = rest.join(' ')


      server.write(new Message({ username, command, contents }).toJSON() + '\n')




    }
    defaultCommand = command


    callback()
  })
