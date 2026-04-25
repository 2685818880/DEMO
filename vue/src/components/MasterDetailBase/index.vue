<template>
  <resize-layout>
    <!-- 主表区域 -->
    <resize-col :default-height="masterHeight">
      <div class="master-detail-container">
        <!-- 主表查询区域 -->
        <search-form
          v-if="config.searchFields"
          :fields="config.searchFields"
          :fields-value="masterParams"
          :visible-num="3"
          @enter="handleMasterSearch"
          @reset="handleMasterReset"
        />

        <!-- 主表操作按钮 -->
        <div v-if="$slots['master-buttons']" class="action-buttons">
          <slot name="master-buttons" />
        </div>

        <!-- 主数据表格 -->
        <DynamicTable
          v-loading="masterLoading"
          :selection-ids.sync="masterSelectionKeys"
          :selection-rows.sync="masterSelectionRows"
          :table-name="config.masterTableName || 'masterTable'"
          :columns="masterColumns"
          :data="masterList"
          :pagination="masterPagination"
          :dblclick="handleMasterRowClick"
          :show-filter="false"
          @size-change="handleMasterSizeChange"
          @page-change="handleMasterPageChange"
          @context-menu-view="handleMasterRowClick"
          v-on="$listeners"
        >
          <!-- 动态列插槽 -->
          <template v-for="col in scopedSlots" v-slot:[col.slotName]="{ row }">
            <slot :name="col.prop" :row="row">
              <slot name="default-cell" :row="row" :column="col">
                {{ getCellValue(row, col.prop) }}
              </slot>
            </slot>
          </template>
        </DynamicTable>
      </div>
    </resize-col>

    <!-- 明细表区域 -->
    <resize-col :default-height="100 - masterHeight">
      <div class="master-detail-container">
        <!-- 明细查询区域 -->
        <div v-if="config.detailSearchFields" class="detail-search">
          <el-form :inline="true" size="small" class="detail-search-form">
            <el-form-item
              v-for="field in config.detailSearchFields"
              :key="field.prop"
              :label="field.label"
            >
              <el-input
                v-if="field.type === 'input' || !field.type"
                v-model="detailParams[field.prop]"
                :placeholder="field.placeholder || '请输入' + field.label"
                style="width: 180px"
                :disabled="field.disableOnNoMaster && !selectedMasterRow"
              />
              <el-select
                v-else-if="field.type === 'enum'"
                v-model="detailParams[field.prop]"
                :placeholder="field.placeholder || '请选择' + field.label"
                style="width: 180px"
                clearable
              >
                <el-option
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleDetailSearch" :loading="detailLoading">
                查询
              </el-button>
              <el-button @click="handleDetailReset" :loading="detailLoading">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 明细数据表格 -->
        <DynamicTable
          v-loading="detailLoading"
          table-name="detailTable"
          :columns="detailColumns"
          :data="detailList"
          :pagination="detailPagination"
          :show-filter="false"
          @size-change="handleDetailSizeChange"
          @page-change="handleDetailPageChange"
        >
          <template v-for="col in detailScopedSlots" v-slot:[col.slotName]="{ row }">
            <slot :name="'detail-' + col.prop" :row="row">
              {{ getCellValue(row, col.prop) }}
            </slot>
          </template>
        </DynamicTable>

        <!-- 空状态提示 -->
        <div v-if="!selectedMasterRow" class="empty-tip">
          <el-empty description="请选择主单据以查看明细" :image-size="100" />
        </div>
      </div>
    </resize-col>
  </resize-layout>
</template>

<script>
export default {
  name: 'MasterDetailBase',

  props: {
    config: {
      type: Object,
      required: true,
      validator(val) {
        if (!val.masterColumns) {
          console.error('[MasterDetailBase] config.masterColumns is required')
          return false
        }
        return true
      }
    },
    apis: {
      type: Object,
      required: true,
      validator(val) {
        if (!val.getMasterList || !val.getDetailList) {
          console.error('[MasterDetailBase] apis.getMasterList and apis.getDetailList are required')
          return false
        }
        return true
      }
    },
    masterHeight: {
      type: Number,
      default: 60
    },
    defaultPageSize: {
      type: Number,
      default: 20
    }
  },

  data() {
    return {
      // 主表
      masterLoading: false,
      masterSelectionKeys: [],
      masterSelectionRows: [],
      masterList: [],
      masterParams: this.initMasterParams(),
      masterPagination: {
        page: 1,
        row: this.defaultPageSize,
        total: 0
      },
      selectedMasterRow: null,

      // 明细表
      detailLoading: false,
      detailList: [],
      detailParams: {},
      detailPagination: {
        page: 1,
        row: this.defaultPageSize,
        total: 0
      }
    }
  },

  computed: {
    masterColumns() {
      return this.config.masterColumns || []
    },
    detailColumns() {
      return this.config.detailColumns || []
    },
    // 主表中需要使用scopedSlot的列
    scopedSlots() {
      return this.masterColumns
        .filter(col => col.scopedSlot)
        .map(col => ({
          prop: col.prop,
          slotName: col.scopedSlot
        }))
    },
    // 明细表中需要使用scopedSlot的列
    detailScopedSlots() {
      return this.detailColumns
        .filter(col => col.scopedSlot)
        .map(col => ({
          prop: col.prop,
          slotName: col.scopedSlot
        }))
    }
  },

  created() {
    this.initDetailParams()
    this.loadMasterData()
  },

  methods: {
    initMasterParams() {
      if (this.config.masterDefaultParams) {
        return { ...this.config.masterDefaultParams }
      }
      return {}
    },

    initDetailParams() {
      const fields = this.config.detailSearchFields || []
      const params = {}
      fields.forEach(f => {
        params[f.prop] = f.defaultValue !== undefined ? f.defaultValue : ''
      })
      this.detailParams = params
    },

    getCellValue(row, prop) {
      if (!prop) return ''
      const keys = prop.split('.')
      let val = row
      for (const key of keys) {
        if (val == null) return ''
        val = val[key]
      }
      return val
    },

    // 主表数据加载
    async loadMasterData() {
      this.masterLoading = true
      try {
        const params = {
          ...this.masterParams,
          page: this.masterPagination.page,
          row: this.masterPagination.row
        }
        const res = await this.apis.getMasterList(params)
        this.masterList = res.data || []
        this.masterPagination.total = res.total || 0
        this.$emit('master-loaded', this.masterList)
        // 清除失效的选中行
        if (this.selectedMasterRow) {
          const keyField = this.config.rowKey || 'id'
          const exists = this.masterList.some(item =>
            item[keyField] === this.selectedMasterRow[keyField]
          )
          if (!exists) {
            this.selectedMasterRow = null
            this.detailList = []
          }
        }
      } catch (error) {
        console.error('加载主表数据失败:', error)
        this.$message.error('加载数据失败')
      } finally {
        this.masterLoading = false
      }
    },

    // 明细表数据加载
    async loadDetailData() {
      if (!this.selectedMasterRow) {
        this.detailList = []
        return
      }
      this.detailLoading = true
      try {
        const keyField = this.config.rowKey || 'id'
        const params = {
          [this.config.detailForeignKey || keyField]: this.selectedMasterRow[keyField],
          ...this.detailParams,
          page: this.detailPagination.page,
          row: this.detailPagination.row
        }
        const res = await this.apis.getDetailList(params)
        this.detailList = res.data || []
        this.detailPagination.total = res.total || 0
        this.$emit('detail-loaded', this.detailList)
      } catch (error) {
        console.error('加载明细数据失败:', error)
        this.$message.error('加载明细数据失败')
      } finally {
        this.detailLoading = false
      }
    },

    // 主表查询
    handleMasterSearch() {
      this.masterPagination.page = 1
      this.loadMasterData()
    },

    // 主表重置
    handleMasterReset() {
      this.masterParams = this.initMasterParams()
      this.masterPagination.page = 1
      this.loadMasterData()
    },

    // 明细查询
    handleDetailSearch() {
      this.detailPagination.page = 1
      this.loadDetailData()
    },

    // 明细重置
    handleDetailReset() {
      this.initDetailParams()
      this.detailPagination.page = 1
      this.loadDetailData()
    },

    // 主表行点击（联动明细）
    handleMasterRowClick(row) {
      this.selectedMasterRow = row
      this.initDetailParams()
      this.detailPagination.page = 1
      this.loadDetailData()
      this.$emit('master-row-click', row)
    },

    // 主表分页
    handleMasterPageChange(page) {
      this.masterPagination.page = page
      this.loadMasterData()
    },

    handleMasterSizeChange(size) {
      this.masterPagination.row = size
      this.masterPagination.page = 1
      this.loadMasterData()
    },

    // 明细分页
    handleDetailPageChange(page) {
      this.detailPagination.page = page
      this.loadDetailData()
    },

    handleDetailSizeChange(size) {
      this.detailPagination.row = size
      this.detailPagination.page = 1
      this.loadDetailData()
    },

    // 刷新主表（外部调用）
    refreshMaster() {
      this.loadMasterData()
    },

    // 刷新明细（外部调用）
    refreshDetail() {
      this.loadDetailData()
    },

    // 刷新全部（外部调用）
    refreshAll() {
      this.loadMasterData()
      this.loadDetailData()
    },

    // 设置主表参数（外部调用）
    setMasterParams(params) {
      this.masterParams = { ...this.masterParams, ...params }
    },

    // 设置选中行（外部调用）
    setSelectedRow(row) {
      this.handleMasterRowClick(row)
    }
  }
}
</script>

<style lang="scss" scoped>
.master-detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;

  .action-buttons {
    padding: 10px 0;
    border-bottom: 1px solid #e6ebf5;
    margin-bottom: 10px;
  }
}

.detail-search {
  padding: 10px 0;
  border-bottom: 1px solid #e6ebf5;
  margin-bottom: 10px;

  .detail-search-form {
    margin-bottom: 0;
  }
}

.empty-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}
</style>
