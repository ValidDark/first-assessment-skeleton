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


function testJSON(text){
    try{
        JSON.parse(text);
        return true;
    }
    catch (error){
        return false;
    }
}



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
          if(testJSON((buffer).toString()))
          {
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
          }
          else{
            this.log(buffer + 'was not valid.')
          }
        })

        server.on('end', () => {
            cli.exec('exit')
            this.log("-----------------------------YOU'VE BEEN DISCONNECTED------------------------------")
        })
    })
    .action(function(input, callback) {
        let entered = words(input, /[\S]+/g)
        let [command, ...rest] = entered
        let contents = rest.join(' ')

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

/////////////////////////////////////////////////////////////                   TRIVIA BOT
    cli
        .mode('botConnect [server] = localhost')
        .delimiter(cli.chalk['green']('connected>'))
        .init(function(args, callback) {
            username = 'Trivia_Bot'




        const filePath = (path.resolve(__dirname, '..\\questions.JSON'))
        const question = (JSON.parse(fs.readFileSync( filePath , 'utf8')))

        let command
        let contents
        var q = -1//what question # we're on.
        var timeleft = 30
        let points = [{name: 'David', points: 0, answer: '' }]


        function intro()
        {
          return function() {
          command = 'broadcast'
          contents = '\nHello, I\'m trivia bot!  every 45 seconds I\'ll ask a multiple choice question!\nYou\'ll have 30 seconds to Direct Message me your answer!\nI\'ll only except the last answer from each person.\nYou can Direct Message me the word \'score\' to get everyones current points!'
          server.write(new Message({
                      username,
                      command,
                      contents
                  }).toJSON() + '\n')
          }
        }

        function timeOver()
        {
          command = 'broadcast'
          contents = 'TIME IS UP!!! answer was: ' + question[q].answer
          server.write(new Message({
                      username,
                      command,
                      contents
                  }).toJSON() + '\n')
        }


        function countDown(x)
        {
          return function() {
          command = 'broadcast'
          contents = x + ' seconds left.'
          server.write(new Message({
                      username,
                      command,
                      contents
                  }).toJSON() + '\n')
          x -= 5
          }
        }

        function askQuestion() {
          return function(){

        command = 'broadcast'

        if(q != -1)
        {

          contents = '.\n................................. \nTIME IS UP!!! answer was: ' + question[q].answer + ' \n.................................. '

          server.write(new Message({
                      username,
                      command,
                      contents
                  }).toJSON() + '\n')

        }

        let ntime =  Date.now()
        let questionValid = true;
        q = Math.floor(Math.random()*question.length)


          contents = '.\n. \n' + question[q].question +'\n A: ' +
                      question[q].A +'\n B: ' +
                      question[q].B +'\n C: ' +
                      question[q].C +'\n D: ' +
                      question[q].D +'\n.  '

          server.write(new Message({
                      username,
                      command,
                      contents
                  }).toJSON() + '\n')

        console.log('Starting Countdown!')

      }



}

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

            setTimeout(intro(), 200)
            setInterval(askQuestion(), 20050)


            server.on('data', (buffer) => {
              if(testJSON((buffer).toString()))
              {
                let message = Message.fromJSON(buffer)
                if (message.command === 'echo') {
                    this.log(cli.chalk.red(Message.fromJSON(buffer).toString()))
                } else if (message.command === 'broadcast') {
                    this.log(cli.chalk.magenta(Message.fromJSON(buffer).toString()))
                } else if (message.command.charAt(0) === '@') {
                    this.log(cli.chalk.blue(Message.fromJSON(buffer).toString()))
                    let found = false

                    if(message.contents.length === 1)
                    {
                    if(/[abcd]/i.test(message.contents))
                    {
                      this.log("IT WAS A B C OR D")
                      for(let x = 0; x < points.length; ++x)
                      {
                        if(message.username === points[x].name)
                        {
                          this.log("NAME IS IN ARRAY")
                          found = true;
                          points[x].answer = contents.charAt(0)
                        }
                      }
                    }
                  }

                    if(message.contents.includes('score'))
                    {
                      this.log("SOMEONE WANTS THEIR SCORE")
                      for(let x = 0; x < points.length; ++x)
                      {
                        if(message.username === points[x].name)
                        {
                          this.log("IT WAS " + message.username)
                          if(points[x].score >= 0)
                          {
                            command = '@' + message.username
                            contents = points[x].score
                            server.write(new Message({
                                username,
                                command,
                                contents
                            }).toJSON() + '\n')

                          }
                          else
                          {
                          points[x].score = 0
                          command = '@' + message.username
                          contents = points[x].score
                          server.write(new Message({
                              username,
                              command,
                              contents
                          }).toJSON() + '\n')
                          }

                        }
                      }
                    }

                    if (found === false)
                    {
                      points.push({
                        name: message.username,
                        points: 0,
                        answer: ''
                                  });
                    }


                    for(let x = 0; x < points.length; ++x)
                    {
                      this.log(points[x])
                    }

                    this.log(cli.chalk.green(Message.fromJSON(buffer).toString()))

                } else {
                    this.log(cli.chalk.green(Message.fromJSON(buffer).toString()))
                    for(let x = 0; x < points.length; ++x)
                    {
                      this.log(points[x])
                    }
                }
              }
              else{
                this.log(buffer + 'was not valid.')
              }
            })

            server.on('end', () => {
                cli.exec('exit')
                this.log("-----------------------------BOT HAS BEEN DISCONNECTED------------------------------")
            })
        })
        .action(function(input, callback) {

            let entered = words(input, /[\S]+/g)

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


/////////////////////////////////////////////////////////////                   FLOOD BOT

        cli
            .mode('floodConnect [server] = localhost')
            .delimiter(cli.chalk['green']('connected>'))
            .init(function(args, callback) {
                let username = 'Flood_Bot'
                let command = 'broadcast'


            function intro()
            {
              return function() {
              let command = 'broadcast'
              let contents = '\nHello, I\'m flood bot!\nI\'m here to test your server!\nThink your client can survive?'
                            server.write(new Message({
                          username,
                          command,
                          contents
                      }).toJSON() + '\n')
              }
            }

            function flood()
            {
              return function() {
              let command = 'broadcast'
              let contents = '\nHello, I\'m flood bot!'

              for(let x = 0; x< 10000; ++x)
              {
                            server.write(new Message({
                          username,
                          command,
                          contents
                      }).toJSON() + '\n')
              }
            }
}



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

                setTimeout(intro(), 200)
                setInterval(flood(), 250)

                server.on('data', (buffer) => {
                  if(testJSON((buffer).toString()))
                  {
                    let message = Message.fromJSON(buffer)
                    if (message.command === 'echo') {
                        this.log(cli.chalk.red(Message.fromJSON(buffer).toString()))
                    } else if (message.command === 'broadcast') {
                        this.log(cli.chalk.magenta(Message.fromJSON(buffer).toString()))
                    } else if (message.command.charAt(0) === '@') {
                        this.log(cli.chalk.blue(Message.fromJSON(buffer).toString()))


                    } else {

                    }
                  }
                  else{
                    this.log(buffer + 'was not valid.')
                  }
                })

                server.on('end', () => {
                    cli.exec('exit')
                    this.log("-----------------------------BOT HAS BEEN DISCONNECTED------------------------------")
                })
            })
            // .action(function(input, callback) {
            //
            //
            //
            //     callback()
            // })
