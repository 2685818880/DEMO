/* eslint-disable */
import Widget from 'arale-widget'
import $ from 'jquery'
import * as d3 from 'd3'

const disptach = d3.dispatch('cellclick')
const HeatMap = Widget.extend({
  attrs: {

    margin: {top: 30, right: 30, bottom: 30, left: 30},

    cellSize: 30,

    min_col_number: {
      value: 1,
      setter: function (val) {
        return parseInt(val) || 1

      }
    },

    //data key，用于判断是否需要重新渲染数据
    dataKeys:["x_pos","y_pos","z_pos"],
    //列
    col_number: {
      value: null,
      setter: function (val) {
        return parseInt(val) || 1
      }
    },

    //行
    min_row_number: {
      value: 1,
      setter: function (val) {
        return parseInt(val) || 1

      }
    },
    row_number: {
      value: null,
      setter: function (val) {
        return parseInt(val) || 1
      }
    },

    // 是否翻转
    reverse: false,

    keys: {x: "y_pos", y: "z_pos"},


    cellData: null
  },

  setup: function () {
    var $element = this.$element = $(this.get("element"))

    if ($element.length == 0) {
      throw Error("容器不存在")
    }

    this.margin = this.get("margin")
    this.cellSize = this.get("cellSize")
    this.minx = this.get("min_row_number")
    this.miny = this.get("min_col_number")
    // this.alias = this.get("alias");
    this.reverse = this.get("reverse")

    this._init()

    //开启 属性的_onRenderxx事件
    this.render()
  },

  _init: function () {
    this.svg = d3.select(this.get("element"))
      .append("svg")

    this.gContainer = this.svg.append('g')
      .attr("transform", "translate(" + (this.margin.left) + "," + (this.margin.top) + ")")

    this.gContainer.append("g").attr('class', 'yLabelContainer')
    this.gContainer.append('g').attr('class', 'xLabelContainer')
    this.gContainer.append("g")
      .attr("class", "cell-content")

    const self = this
    disptach.on('cellclick', (cell) => {
      var mousedownFn = this.get("mousedown")
      const res = []
      if (cell) {
        d3.select(cell).each(item => res.push(item))
      } else {
        this.getSelectedCell().each(item => res.push(item))
      }
      mousedownFn && mousedownFn.call(self, res)
    })
    // this.svg.select(".cell-content").on("click.cell", (d) => {
    //
    // })

    this.$element.css('position', 'relative').append('<div id="heatmap-tooltip" class="hidden heatmap-tooltip">')
  },

  _setWidth (val) {
    var self = this
    this.svg.attr("width", self.cellSize * val + (self.margin.left + self.margin.right))
  },

  _setHeight (val) {
    var self = this
    this.svg.attr("height", self.cellSize * val + (self.margin.top + self.margin.bottom))
  },

  _onRenderReverse (val) {
    this.reverse = val
    this._onRenderRow_number(this.get('row_number'))
  },

  _onRenderRow_number: function (val) {
    this.maxx = this.get("row_number")
    var self = this,
      reverse = this.get("reverse"),
      translatex = self.cellSize / 2
      this.minx = this.get("min_row_number")
    this.svg.selectAll(".xLabel").remove()
    var xLabel = this.svg.selectAll('.xLabelContainer')
      .selectAll(".xLabel")
      .data(reverse ? d3.range(self.minx, val + 1).reverse() : d3.range(self.minx, val + 1))
      .enter()
      .append("text")
      .text(function (d) {
        return +d
      })
      .attr("x", function (d, i) {
        return self.cellSize * (i )
      })
      .attr("class", function (d, i) {
        return "xLabel" + " x" + d
      })
      .style("text-anchor", 'middle')
      .attr("transform", "translate(" + translatex + ",20" + ")")

    xLabel.exit().remove()

    this._setColsTransform()
    this._setWidth(val)
  },

  _onRenderCol_number: function (val) {

    this.maxy = this.get("col_number")
    var self = this,
      reverse = this.get("reverse")

    this.miny = this.get("min_col_number")
    self.svg.selectAll(".yLabel").remove()
    var yLabel = self.svg.selectAll(".yLabelContainer")
      .selectAll(".yLabel")
      .data(d3.range(self.miny, val + 1).reverse())
      .enter()
      .append("text")
      .text(function (d) {
        return +d
      })
      .attr('y', function (d, i) {
        return self.cellSize * i
      })
      .attr("class", function (d, i) {
        return "yLabel " + "y" + d
      })
      .style("text-anchor", reverse ? "start" : "end")

    yLabel.exit().remove()
    this._setRowsTransform()
    this._setHeight(val)
  },

  _setRowsTransform () {
    var self = this,
      translatey = self.cellSize * (self.maxy + 1 - self.miny )

    this.svg.selectAll('.xLabelContainer')
      .attr("transform", "translate(" + 0 + "," + translatey + ")")
  },

  _setColsTransform () {
    var self = this,
      reverse = this.get("reverse"),
      x = reverse ? self.cellSize * (self.maxx + 1 - self.minx) : 0,
      translatex = reverse ? self.margin.left / 2 : -self.margin.right / 2,
      translatey = self.cellSize / 1.5
    this.svg.selectAll('.yLabelContainer')
      .attr("transform", "translate(" + (translatex + +x) + "," + translatey + ")")
  },

  _onRenderCellData: function (val) {
    var self = this,
      svg = self.svg,
      keys = self.get("keys"),
      x = keys.x,
      y = keys.y,
      maxy = self.maxy,
      maxx = self.maxx,
      minx = self.minx,
      reverse = self.get("reverse"),
      datakeys = self.get("dataKeys")
    var cellLabel = self.get('cellLabel')
    var cellContent = self.svg.select(".cell-content")

    var cards = cellContent.selectAll(".cellContainer")
    .data(val, function (d) {
      return  datakeys.map(function(item){
        return d[item]
      }).join(",")
    })

    var enterCards = cards.enter()
    .append('g')
    .attr('class', 'cellContainer')
    .attr('x', function (d) {
      return (reverse ? (maxx - d[x] ) : (d[x] - minx) ) * self.cellSize
    })
    .attr('y', function (d) {
      return (maxy - d[y]) * self.cellSize
    })
    .attr('transform', function (d) {
      return "translate(" + ((reverse ? (maxx - d[x] ) : (d[x] - minx) ) * self.cellSize) + ","
        + ((maxy - d[y]) * self.cellSize) + ")"
    })
    .on("mouseover.highlight", function (d) {
      d3.select(this).classed("cell-hover", true)

      svg.selectAll(".xLabel").classed("text-highlight", function (r, ri) {
        return ri == (reverse ? (maxx - d[x]) : (d[x] - minx))
      })

      svg.selectAll(".yLabel").classed("text-highlight", function (c, ci) {
        return ci == ((maxy - d[y]))
      })
    }).on("mouseleave.highlight", function () {

      d3.select(this).classed("cell-hover", false)
      svg.selectAll(".yLabel").classed("text-highlight", false)
      svg.selectAll(".xLabel").classed("text-highlight", false)
      //d3.select("#tooltip").classed("hidden", true);

        d3.event.stopPropagation()
      }, false).on("mousedown.highlight", function (d) {
        if(!self.get('drag')) {
          var mousedownFn = self.get("mousedown")
          mousedownFn && mousedownFn.call(self, d)
        }
      })

    this.rects = enterCards
      .append("rect")
      .attr("class", "cell bordered")
      .attr("width", self.cellSize - 0.5)
      .attr("height", self.cellSize - 0.5)
      .style("fill", "#fff")

    if (cellLabel) {
      var cellLabelColor = self.get('cellLabelColor')
      enterCards.append('text')
        .text(cellLabel)
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'middle')
        .attr('x', self.cellSize / 2)
        .attr('y', self.cellSize / 2 + 15)
        .attr('fill', cellLabelColor || '#fff')
        .attr('width', '40px')


    }

    cards.exit().remove()

    if(this.get('toolTip')) {
      this.showToolTip()
    }

    //设置图标信息
    var iconFilter = self.get('iconFilter')
    self.setIcon(iconFilter)

    // self.setFillByLegend();
    self.setFill(self.get("fill"))

    if (self.get("drag")) {
      self.drag()
    }

    self.trigger("shown.cell")
  },

  //设置填充色
  setFill: function (fill) {
    var self = this

    //设置填充色
    self.rects && self.rects.transition().duration(1000)
      .style("fill", function (d) {
        if (typeof fill === 'function') {
          return fill(d) || '#fff'
        } else {
          return fill
        }
      })
  },

  setIcon: function (iconFilter) {
    var self = this
    var iconsFn = self.get('icons')
    if (iconsFn) {
      const iconColor = self.get('iconColor')
      var cellContent = self.svg.select(".cell-content")

      cellContent.selectAll(".cellContainer").filter(iconFilter).each(function (d, i) {
        let icons = iconsFn(d)
        Array.isArray(icons) || (icons = [icons])
        const selection = this
        icons.forEach(function (icon) {
          d3.select(selection).append("use")
            .attr("xmlns:xlink", "http://www.w3.org/1999/xlink")
            .attr("xlink:href", icon)
            .attr('width', 20)
            .attr('height', 20)
            .attr('x', self.cellSize / 2 - 20 / 2)
            .attr('y', 5)
            .attr('fill', (d) => iconColor(d, icon))
        })
      })
    } else {
      var icon = self.get('icon')
      var cellContent = self.svg.select(".cell-content")

      cellContent.selectAll(".cellContainer").filter(iconFilter)
        .append("use")
        .attr("xmlns:xlink", "http://www.w3.org/1999/xlink")
        .attr("xlink:href", icon)
        .attr('width', 20)
        .attr('height', 20)
        .attr('x', self.cellSize / 2 - 20 / 2)
        .attr('y', 5)
        .attr('fill', 'red')
    }
  },

  //拖拽事件
  drag: function () {
    var self = this,
      svg = self.svg,
      keys = self.get("keys"),
      xkey = keys.x,
      ykey = keys.y

    let curMouseDownCell = null
    var cell = svg.select(".cell-content").on("mousedown.cell", function () {
      var p = d3.mouse(this)
      curMouseDownCell = d3.event.target

      //支持alt
      if (!d3.event.altKey) {
        self.resetSelected()
      }

      cell.append("rect")
        .attr("x", p[0])
        .attr("y", p[1])
        .attr("width", 1)
        .attr("height", 1)
        .attr("class", "selection")
    }).on("mousemove.cell", function () {
      var s = cell.select(".selection")
      curMouseDownCell = null

      if (!s.empty()) {
        var p = d3.mouse(this),
          d = {
            x: parseInt(s.attr("x"), 10),
            y: parseInt(s.attr("y"), 10),
            width: parseInt(s.attr("width"), 10),
            height: parseInt(s.attr("height"), 10)
          },
          move = {
            x: p[0] - d.x,
            y: p[1] - d.y
          }

        if (move.x < 1 || (move.x * 2 < d.width)) {
          d.x = p[0]
          d.width -= move.x
        } else {
          d.width = move.x
        }

        if (move.y < 1 || (move.y * 2 < d.height)) {
          d.y = p[1]
          d.height -= move.y
        } else {
          d.height = move.y
        }

        for (var key in d) {
          s.attr(key, d[key])
        }

        svg.selectAll('.cell-selection.cell-selected').classed("cell-selected", false)
        svg.selectAll(".text-selection.text-selected").classed("text-selected", false)

        svg.selectAll('.cell').filter(function (cell_d, i) {
          const parentNode = this.parentNode
          const x = +parentNode.getAttribute('x')
          const y = +parentNode.getAttribute('y')
          if (
            !d3.select(this).classed("cell-selected") &&
            (x) + self.cellSize >= d.x && (x) <= d.x + d.width &&
            (y) + self.cellSize >= d.y && (y) <= d.y + d.height
          ) {

            d3.select(this)
              .classed("cell-selection", true)
              .classed("cell-selected", true)

            svg.select(".x" + (cell_d[xkey]))
              .classed("text-selected", true)

            svg.select(".y" + (cell_d[ykey]))
              .classed("text-selected", true)
          }
        })
      }
      return false
    }).on("mouseup.cell", function () {
      // remove selection frame
      svg.selectAll("rect.selection").remove()
      svg.selectAll('.cell-selection').classed("cell-selection", false)
      disptach.call('cellclick', this, curMouseDownCell)
    }).on("mouseleave.cell", function () {
        svg.selectAll("rect.selection").remove()
        svg.selectAll('.cell-selection').classed("cell-selection", false)
      })
  },

  //获取选中
  getSelectedCell: function () {
    return this.svg.selectAll(".cell-selected")
  },

  //重置选中
  resetSelected: function () {
    this.svg.selectAll(".cell-selected").classed("cell-selected", false)
    this.svg.selectAll(".xLabel").classed("text-selected", false)
    this.svg.selectAll(".yLabel").classed("text-selected", false)
  },

  setSelected () {

  },

  showToolTip: function () {
    var self = this
    const containerOffset = this.$element.offset()
    var $window = $(window)

    this.svg.selectAll('.cellContainer').on('mouseover', function (d) {
      var $this = d3.select(this)

      var x = $this.attr('x')
      var y = $this.attr('y')
      var x1 = d3.event.offsetX
      var y1 = d3.event.offsetY
      var innerX = x1 - x + 10 - self.margin.left
      var innerY = y1 - y - 10 - self.margin.top

      var winW = $window.width()

      var val
      var toolTipContent = self.get('toolTipContent')
      if (typeof toolTipContent === 'function') {
        val = toolTipContent(d)
      } else {
        val = toolTipContent
      }
      const $heatmapTooltip = $('#heatmap-tooltip')
      $heatmapTooltip.html(val)
      d3.select("#heatmap-tooltip").classed("hidden", false);

      var left = d3.event.pageX + self.$element.scrollLeft() - containerOffset.left - innerX
      if (winW < (d3.event.pageX + 200)) {
        left = left - 200
      }

      var top = d3.event.pageY - containerOffset.top - innerY + self.cellSize
      const tipHeight = $heatmapTooltip.height()
      if (self.$element.height() - top < tipHeight) {
        top = y - tipHeight + 20
      }

      d3.select("#heatmap-tooltip")
        .style("left", left + "px")
        .style("top", top + "px")
        .select("#value")
    }).on('mouseleave', function () {
      d3.select("#heatmap-tooltip").classed("hidden", true);
    })
  },

  destroy () {
    this.$element.remove()
    HeatMap.superclass.destroy.call(this)
  }
})

export default HeatMap
