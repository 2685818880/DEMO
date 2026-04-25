<script>
  import treeToArray from './eval'
  import DynamicTable from './index'

  export default {
    components: {
      DynamicTable
    },
    props: {
      data: {
        type: Array,
        default: () => []
      },
      columns: {
        type: Array,
        default: () => []
      },
      expandAll: {
        type: Boolean,
        default: true
      },
      isFormatted: {
        type: Boolean,
        default: false
      }
    },
    computed: {
      formatData() {
        if (this.isFormatted) {
          return this.data
        }
        return treeToArray(this.data, this.expandAll)
      }
    },
    methods: {
      iconShow(col, row) {
        return col.treeIcon && row.children && row.children.length > 0
      },
      toggleExpand(row) {
        row._expand = !row._expand
      },
      showRow({row}) {
        const show = row.parent ? row.parent._expand : true

        row._expand = show && row._expand
        return show ? 'animation: treeTableShow 1s' : 'display: none'
      }
    },
    render() {
      return (
        <dynamic-table
          data={this.formatData}
          row-style={this.showRow}
          columns={this.columns}
          {...{attrs: this.$attrs}}
          scopedSlots={
            {
              'tree-table-slot': ({row, column, col, index}) => {
                return (
                  <span>
                    {
                      Array.from({length: row._level}).map((space, _index) => (
                        <span key={_index} class="ms-tree-space" v-show={col.treeIcon}/>
                      ))}
                    <span
                      class="tree-ctrl"
                      onClick={() => this.toggleExpand(row)}
                      v-show={this.iconShow(col, row)}>
                      {row._expand ? <span class="el-icon-minus"/> : <span class="el-icon-plus"/>}
                    </span>
                  </span>
                )
              },
              ...this.$scopedSlots
            }
          }
      >
      </dynamic-table>
      )
    }
  }
</script>

<style rel="stylesheet/css">
  @keyframes treeTableShow {
    from {opacity: 0;}
    to {opacity: 1;}
  }
</style>

<style lang="scss" rel="stylesheet/scss" scoped>
  $color-blue: #2196F3;
  $space-width: 12px;
  .ms-tree-space {
    position: relative;
    top: 1px;
    display: inline-block;
    font-style: normal;
    font-weight: 400;
    line-height: 1;
    width: $space-width;
    height: 14px;
    &::before {
      content: ""
    }
  }
  .processContainer{
    width: 100%;
    height: 100%;
  }
  table td {
    line-height: 26px;
  }

  .tree-ctrl{
    position: relative;
    cursor: pointer;
    color: $color-blue;
    margin-left: -$space-width;
  }
</style>
