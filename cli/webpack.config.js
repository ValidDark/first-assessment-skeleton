module.exports = {
  target: 'node',

  node: {
    __dirname: false,
    __filename: false,
  },

  entry: {
    app: ['babel-polyfill', './src/app.js']
  },

  output: {
    path: './dist/',
    filename: '[name].js'
  },

  module: {
    loaders: [{
      test: /.js$/,
      exclude: /node_modules/,
      loader: 'babel'
    }]
  }
}
