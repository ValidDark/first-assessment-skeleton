import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let defaultCommand = ''

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

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
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    let entered = words(input, /\S*\s*/)
    let [ command, ...rest ] = entered
    const contents = rest.join(' ')

    console.log(entered)
    console.log(command.charAt(0))

    console.log("start of statement default command : " + defaultCommand)

    console.log("AT 0 :   " + command.charAt(0))

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      console.log(JSON.stringify({ username, command, contents }))

    } else if (command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
      console.log(JSON.stringify({ username, command, contents }))

    } else if (command.charAt(0) === '@') {
      command = command.charAt(0)



      const [ ...rest ] = entered
      const contents = rest.join(' ')

      console.log("content: " + contents)

      server.write(new Message({ username, command, contents }).toJSON() + '\n')

    } else if (command === 'users') {
      server.write(new Message({ username, command }).toJSON() + '\n')
    } else {
      command = defaultCommand
      const [ ...rest ] = entered
      const contents = rest.join(' ')
      console.log('username: ' + username + ' command: ' + defaultCommand + ' contents: ' + contents)

      server.write(new Message({ username, command, contents }).toJSON() + '\n')

      console.log(JSON.stringify({ username, command, contents }))



    }
    defaultCommand = command

    console.log("type of command done : " + defaultCommand)

    callback()
  })
