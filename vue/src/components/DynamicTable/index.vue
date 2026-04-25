<template>
  <div class="dynamic-table">
    <el-popover
      v-if="dynamicColumn"
      popper-class="dynamic-column"
      placement="left-start"
      width="100%"
      trigger="click">
      <el-checkbox-group v-model="checkList">
        <el-checkbox
          v-for="(item,index) in columns"
          :key="index"
          :label="item.label"></el-checkbox>
      </el-checkbox-group>
      <el-button
        slot="reference"
        type="primary"
        icon="el-icon-setting"
        size="mini"></el-button>
    </el-popover>
    <pagination
      v-if="pagination && (paginationPlacement === 'both' || paginationPlacement === 'top' )"
      :class="['g-pagination', paginationAlign]"
      :current-page="pagination.page"
      :page-size="pagination.row"
      :total="pagination.total"
      @current-change="handlePageChange"
      @size-change="handleSizeChange">
    </pagination>

    <el-table
      :data="data"
      :border="border"
      @row-dblclick="handleDblclick"
      @select="handleSelect"
      @select-all="handleSelectAll"
      :ref="tableName"
      v-bind="$attrs"
      :row-class-name="rowClassName"
      @expand-change="handleExpand"
      @row-contextmenu="handleContextMenu"
      v-on="$listeners"
      height="100%"
    >
      <template v-if="isShowExpand">
        <el-table-column
          type="expand"
          v-if="expandType === 'form'"
          fixed="left">
          <form-view
            slot-scope="{ row }"
            :fields-value="row"
            :fields-desc="columns"
            style="width: calc(100vw - 340px)" />
        </el-table-column>
        <el-table-column
          type="expand"
          v-if="expandType === 'subTable'">
          <template slot-scope="{ row }">
            <div class="subTable">
              <slot
                name="subTable"
                :row="row"></slot>
            </div>
          </template>
        </el-table-column>
      </template>
      <template
        v-for="(col, index) in showColumns">
        <template v-if="col.key === 'action' && !col.scopedSlot">
          <el-table-column
            :label="col.label"
            v-if="optType === 'click' || optType === 'both'"
            :key="index"
            v-bind="col.attrs">
            <template
              v-if="col.options"
              slot-scope="{row}">
              <template v-if="col.options.length < 2">
                <a
                  v-for="(option, index) in col.options"
                  :key="index"
                  @click="handleContextMenuChange(option.command, row)">
                  {{ option.label }}
                </a>
              </template>
              <el-dropdown v-else>
                <span class="el-dropdown-link">
                  操作<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    v-for="(option, index) in col.options"
                    :key="index"
                    @click.native="handleContextMenuChange(option.command, row)">
                    <a>{{ option.label }}</a>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </template>
        <template v-else>
          <el-table-column
            v-if="!col.type"
            :prop="col.prop"
            :key="col.key"
            :label="$t(col.label)"
            :width="col.width"
            v-bind="col.attrs"
            :render-header="col.renderHeader">
            <template slot-scope="{row, column, $index}">
              <slot
                name="tree-table-slot"
                :row="row"
                :index="index"
                :col="col"
                :column="column"
                :rowIndex="$index" />
              <slot
                v-if="col.scopedSlot"
                :name="col.scopedSlot"
                :column="column"
                :row="row"
                :rowIndex="$index"></slot>
              <template v-else>{{ row[col.prop] ? col.format ? col.format(row[col.prop]) : row[col.prop] : '-' }}
              </template>
            </template>
          </el-table-column>
          <el-table-column
            v-else
            :type="col.type"
            v-bind="col.attrs"
            v-on="col.listeners"
            :key="col.key"
            :width="col.width">
          </el-table-column>
        </template>
      </template>
    </el-table>
    <div class="tools">
      <slot name="tools">
        <div style="width: 10px; height: 10px"></div>
      </slot>
      <pagination
        v-if="pagination && (paginationPlacement === 'both' || paginationPlacement === 'bottom')"
        :class="['g-pagination', paginationAlign]"
        :current-page="pagination.page"
        :page-size="pagination.row"
        :total="pagination.total"
        @current-change="handlePageChange"
        @size-change="handleSizeChange">
      </pagination>
    </div>
  </div>
</template>

<script>
import Pagination from '@comp/pagination'
import FormView from '@comp/formview'

export default {
  name: 'DynamicTable',

  components: {
    FormView,
    Pagination
  },

  props: {
    optType: {
      default: 'contextMenu',
      validator(value) {
        return ['contextMenu', 'click', 'both'].indexOf(value) > -1
      }
    },
    border: {
      default: true
    },
    mainColLen: {
      default: 10,
      type: Number
    },
    isShowExpand: {
      type: Boolean,
      default: false
    },
    expandType: {
      default: 'form',
      validator(value) {
        return ['form', 'subTable'].indexOf(value) > -1
      }
    },
    data: {
      type: Array,
      default() {
        return []
      }
    },
    columns: {
      type: Array,
      default() {
        return []
      }
    },
    // id字段
    keyname: {
      type: String,
      default: 'id'
    },
    selectionIds: {
      type: Array,
      default: function () {
        return []
      }
    },
    rowCheck: {
      type: Function,
      default: function (val) {
      }
    },
    dblclick: {
      type: Function,
      default: function () {
      }
    },
    tableName: {
      type: String
    },
    dynamicColumn: {
      type: Boolean,
      default: false
    },
    pagination: {
      default: false,
      validator(val) {
        if (val === false || typeof val === 'object') {
          return true
        }
        return false
      }
    },
    paginationAlign: {
      default: 'right',
      validator(value) {
        return ['right', 'center', 'left'].indexOf(value) > -1
      }
    },
    paginationPlacement: {
      default: 'bottom',
      validator(value) {
        return ['both', 'top', 'bottom'].indexOf(value) > -1
      }
    }
  },
  data() {
    return {
      // 动态列选中数组
      checkList: []
    }
  },

  computed: {
    showColumns() {
      return this.columns.filter(item => !item.hidden)
    },
    isContextMenu() {
      return this.optType === 'contextMenu' || this.optType === 'both'
    },
    contextMenuOptions() {
      const actionOption = this.columns.find(item => item.key === 'action') || {}
      return actionOption.options || []
    }
  },

  created() {
    this.updateCheckList()
  },

  methods: {
    handleDblclick(row, column) {
      this.dblclick(row)
    },

    rowClassName({ row }) {},

    handleSelect(val) {
      this.filterPointId(val)
      this.rowCheck(val)
    },

    handleSelectAll(val) {
      this.filterPointId(val)
      this.rowCheck(val)
    },

    filterPointId(val) {
      const selectionIds = this.selectionIds.slice()
      const id = this.keyname
      const valIds = val.map(item => item[id])

      this.data.forEach((item) => {
        const selectionIdsIndexOf = selectionIds.indexOf(item[id])
        if (selectionIdsIndexOf > -1) {
          if (valIds.indexOf(item[id]) === -1) {
            selectionIds.splice(selectionIdsIndexOf, 1)
          }
        } else {
          if (valIds.indexOf(item[id]) > -1) {
            selectionIds.push(item[id])
          }
        }
      })
      this.$emit('update:selectionIds', selectionIds)
    },

    handleExpand(row) {
      if (!row.isExpand) {
        row.isExpand = true
        this.$emit('expand-change', row)
      }
    },

    updateCheckList() {
      const array = []
      for (const item of this.columns) {
        if (Object.prototype.hasOwnProperty.call(item, 'hidden')) {
          if (item.hidden) continue
        }
        array.push(item.label)
      }
      this.checkList = array
    },

    handleRowClick(row) {
    },

    updateRowSelection() {
      this.$nextTick(() => {
        if (this.$refs[this.tableName]) {
          const key = this.keyname
          this.data.forEach((item, index) => {
            this.$refs[this.tableName].toggleRowSelection(item, this.selectionIds.indexOf(item[key]) > -1)
          })
        }
      })
    },

    handlePageChange(val) {
      this.$emit('page-change', val)
    },

    handleSizeChange(val) {
      this.$emit('size-change', val)
    },

    handleContextMenu(row, column, event) {
      let { isContextMenu, contextMenuOptions } = this
      if (!isContextMenu) {
        return
      }

      if (contextMenuOptions.length === 0) {
        // eslint-disable-next-line
        console.warn('右键菜单选项配置为空')
        return
      }

      event.preventDefault()
      event.stopPropagation()
      const { pageX, pageY } = event
      if (typeof contextMenuOptions === 'function') {
        contextMenuOptions = contextMenuOptions(row)
      }

      this.$_contextMenu.show({
        position: {
          top: pageY,
          left: pageX
        },
        content: (h) => {
          return (
            <context-menu
              options={ contextMenuOptions }
              onChange={ (command) => this.handleContextMenuChange(command, row) }>
            </context-menu>
          )
        }
      })
    },

    handleContextMenuChange(command, row) {
      this.$emit(`context-menu-${command}`, row)
    }
  },

  beforeUpdate() {
    this.$nextTick(() => {
      if (this.$refs[this.tableName]) {
        const key = this.keyname
        this.data.forEach((item, index) => {
          this.$refs[this.tableName].toggleRowSelection(item, this.selectionIds.indexOf(item[key]) > -1)
        })
      }
    })
  },

  watch: {
    checkList() {
      this.columns.forEach((item) => {
        item.hidden = !this.checkList.includes(item.label)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.tools {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
}

.dynamic-table {
  display: flex;
  flex: 1;
  flex-direction: column;
  overflow: auto;
}

.el-popover.el-popper.dynamic-column {
  .el-checkbox + .el-checkbox {
    margin-left: 0;
  }

  .el-checkbox {
    display: block;
  }
}
</style>
