const fs = require('fs')



export class Question {
    constructor(newObj) {

      this.question = newObj.question
      this.A = newObj.A
      this.B = newObj.B
      this.C = newObj.C
      this.D = newObj.D
      this.answer = newObj.answer
    }

    static parseFromFilePath(filePath) {

        return new Question (JSON.parse(fs.readFileSync(filePath, 'utf8')))
    }


}
