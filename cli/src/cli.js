import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

import util from 'util'
import path from 'path'
import fs from 'fs'
import { Question } from './Question'


export const cli = vorpal()

let username
let server
let defaultCommand = ''
let lastPMd = '' // the last person private messaged

cli
    .delimiter(cli.chalk['yellow']('$>')) //what is shown before you type

cli
    .mode('connect <username> [server] = localhost')
    .delimiter(cli.chalk['green']('connected>'))
    .init(function(args, callback) {
        username = args.username
        server = connect({
            host: args.server,
            port: 8080
        }, () => {

            server.write(new Message({
                username,
                command: 'connect'
            }).toJSON() + '\n')
            callback()
        })

        server.on('data', (buffer) => {
            let message = Message.fromJSON(buffer)
            if (message.command === 'echo') {
                this.log(cli.chalk.red(Message.fromJSON(buffer).toString()))
            } else if (message.command === 'broadcast') {
                this.log(cli.chalk.magenta(Message.fromJSON(buffer).toString()))
            } else if (message.command === '@') {
                this.log(cli.chalk.blue(Message.fromJSON(buffer).toString()))
            } else {
                this.log(cli.chalk.green(Message.fromJSON(buffer).toString()))
            }
        })

        server.on('end', () => {
            cli.exec('exit')
            this.log("-----------------------------YOU'VE BEEN DISCONNECTED------------------------------")
        })
    })
    .action(function(input, callback) {
        let entered = words(input, /[\S]+/g)

        console.log(input)
        console.log(entered)



        let [command, ...rest] = entered
        let contents = rest.join(' ')

        console.log(contents)

        if (command === 'disconnect') {
            server.end(new Message({
                username,
                command
            }).toJSON() + '\n')
        } else if (command === 'echo') {
            server.write(new Message({
                username,
                command,
                contents
            }).toJSON() + '\n')

        } else if (command === 'broadcast' || command === 'say') {
            command = 'broadcast'
            server.write(new Message({
                username,
                command,
                contents
            }).toJSON() + '\n')

        } else if (command.charAt(0) === '@') {

            server.write(new Message({
                username,
                command,
                contents
            }).toJSON() + '\n')

        } else if (command === 'users') {
            server.write(new Message({
                username,
                command
            }).toJSON() + '\n')
        } else {
            command = defaultCommand
            const [...rest] = entered
            let contents = rest.join(' ')


            server.write(new Message({
                username,
                command,
                contents
            }).toJSON() + '\n')




        }
        defaultCommand = command


        callback()
    })




































    cli
        .mode('botConnect [server] = localhost')
        .delimiter(cli.chalk['green']('connected>'))
        .init(function(args, callback) {
            username = 'Trivia_Bot'


console.log(__dirname)

        const filePath = (path.resolve(__dirname, '..\\questions.JSON'))
        const question = (JSON.parse(fs.readFileSync( filePath , 'utf8')))

        function askQuestion() {
          var q = Math.floor(Math.random()*question.length);
            console.log(question[q].question)
        }


console.log(question[0])
console.log(question[1].question)

setInterval(askQuestion, 2000);

//         console.log(question)

            server = connect({
                host: args.server,
                port: 8080
            }, () => {




                server.write(new Message({
                    username,
                    command: 'connect'
                }).toJSON() + '\n')
                callback()
            })

            server.on('data', (buffer) => {
                let message = Message.fromJSON(buffer)
                if (message.command === 'echo') {
                    this.log(cli.chalk.red(Message.fromJSON(buffer).toString()))
                } else if (message.command === 'broadcast') {
                    this.log(cli.chalk.magenta(Message.fromJSON(buffer).toString()))
                } else if (message.command === '@') {
                    this.log(cli.chalk.blue(Message.fromJSON(buffer).toString()))
                } else {
                    this.log(cli.chalk.green(Message.fromJSON(buffer).toString()))
                }
            })

            server.on('end', () => {
                cli.exec('exit')
                this.log("-----------------------------BOT HAS BEEN DISCONNECTED------------------------------")
            })
        })
        // .action(function(input, callback) {
        //
        //   setInterval(testFunc, 2000);
        //
        //     let entered = words(input, /[\S]+/g)
        //
        //     let [command, ...rest] = entered
        //     let contents = rest.join(' ')
        //
        //     console.log(contents)
        //
        //     if (command === 'disconnect') {
        //         server.end(new Message({
        //             username,
        //             command
        //         }).toJSON() + '\n')
        //     } else if (command === 'echo') {
        //         server.write(new Message({
        //             username,
        //             command,
        //             contents
        //         }).toJSON() + '\n')
        //
        //     } else if (command === 'broadcast' || command === 'say') {
        //         command = 'broadcast'
        //         server.write(new Message({
        //             username,
        //             command,
        //             contents
        //         }).toJSON() + '\n')
        //
        //     } else if (command.charAt(0) === '@') {
        //
        //         server.write(new Message({
        //             username,
        //             command,
        //             contents
        //         }).toJSON() + '\n')
        //
        //     } else if (command === 'users') {
        //         server.write(new Message({
        //             username,
        //             command
        //         }).toJSON() + '\n')
        //     } else {
        //         command = defaultCommand
        //         const [...rest] = entered
        //         let contents = rest.join(' ')
        //
        //
        //         server.write(new Message({
        //             username,
        //             command,
        //             contents
        //         }).toJSON() + '\n')
        //
        //
        //
        //
        //     }
        //     defaultCommand = command
        //

        //     callback()
        // })
