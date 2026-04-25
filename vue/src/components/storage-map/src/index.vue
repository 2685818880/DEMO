<template>
  <div class="heatmap-container"></div>
</template>

<script>
  import Heatmap from './heatmap'

  export default {
    name: 'StorageMap',

    props: {
      isReverse: {
        default: false
      },
      maxX: {
        default: 0
      },
      maxY: {
        default: 0
      },
      minX: {
        default: 0
      },
      minY: {
        default: 0
      },
      // 刷新字段
      dataKeys: {
        required: true
      },
      formatCellLabel: {
        type: Function
      },
      cellLabelColor: {
        type: Function
      },
      icon: {
        type: [String, Function]
      },
      icons: {
        type: [String, Function]
      },
      iconFilter: {
        type: Function
      },
      iconColor: {
        type: Function
      },
      toolTip: {
        default: false
      },
      toolTipContent: {
        type: [String, Function]
      },
      fill: {
        type: Function
      },
      list: {
        type: Array,
        required: true
      },
      drag: {
        type: Boolean,
        required: false
      }
    },

    watch: {
      list: {
        deep: true,
        handler: 'initHeatMap'
      }
    },

    beforeDestroy() {
      this.heatMap && this.heatMap.destroy()
      this.heatMap = null
    },

    methods: {
      initHeatMap() {
        if (!this.heatMap) {
          const {
            $el, maxX, maxY, minX, minY, isReverse, dataKeys,
            formatCellLabel, cellLabelColor, icon, iconFilter,
            iconColor, toolTipContent, toolTip, list,
            icons, drag,
            fill
          } = this
          this.heatMap = new Heatmap({
            element: $el,
            cellSize: 45,
            row_number: maxX,
            col_number: maxY,
            min_row_number: minX,
            min_col_number: minY,
            reverse: isReverse,
            toolTip,
            dataKeys,
            cellLabel: formatCellLabel,
            cellLabelColor: cellLabelColor,
            icon,
            icons,
            iconFilter: iconFilter,
            toolTipContent,
            fill,
            cellData: list,
            iconColor,
            drag,
            mousedown: (d) => {
              this.$emit('cell-click', d)
            }
          })
        }

        this.setCellData()
      },

      setCellData() {
        this.heatMap.set('cellData', this.list)
      },
      getSelectedCell() {
        return this.heatMap.getSelectedCell()
      },
      resetSelected() {
        return this.heatMap.resetSelected()
      }
    }
  }
</script>

<style>
@import './index.css';
</style>
