const path = require('path')
const webpack = require('webpack')
const config = require('../config')
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')
const staticPath = path.join(__dirname, '../static', )
const ExtractTextPlugin = require('extract-text-webpack-plugin')

module.exports = {
  entry: {
    dll: [
      'vue',
      'vue-router',
      'vuex',
      'element-ui',
      'element-ui/lib/theme-chalk/index.css'
    ]
  },
  output: {
    path: staticPath,
    filename: '[name].[chunkhash:7].js',
    library: 'dll_library'
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({
          fallback: "style-loader",
          use: "css-loader"
        })
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: 'fonts/[name].[hash:7].[ext]'
        }
      }
    ]
  },
  plugins: [
    new ExtractTextPlugin({
      filename: 'dll.[contenthash:7].css'
    }),
    new UglifyJsPlugin({
      uglifyOptions: {
        compress: {
          warnings: false
        }
      },
      sourceMap: config.build.productionSourceMap,
      parallel: true
    }),
    new webpack.DllPlugin({
      path: path.join(__dirname, '.', 'dll-manifest.json'),
      name: 'dll_library'
    })
  ]
}
