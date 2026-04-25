<template>
  <drawer-modal
    :title="i18n('手动出库')"
    :visible.sync="visible"
    >
    <search-form :fields="searchFields" :fields-value="params" :visible-num="3">
      <el-button type="primary" icon="el-icon-search" @click="handleSearch">{{i18n('查询')}}</el-button>
      <el-button icon="el-icon-refresh"  @click="handleReset">{{i18n('重置')}}</el-button>
    </search-form>

    <dynamic-table
      v-loading="loading"
      :columns="columns"
      :data="list"
      :selectionIds.sync="selectionKeys"
      :tableName="tableName"
      :pagination="pagination"
      @size-change="handleSizeChange"
      @page-change="handlePageChange">
    </dynamic-table>

    <div slot="footer">
      <el-button :loading="loading" type="primary" class="buttonStyle" @click="handleSubmit">{{i18n('确定')}}</el-button>
      <el-button plain class="buttonStyle" @click="handleCancel">{{i18n('取消')}}</el-button>
    </div>
  </drawer-modal>
</template>

<script>
  import { queryStorageAllocate } from '@/api/warehouse/index'
  import { pagedSearchMixin, formMixin } from '@/mixins'
  import { getEnumLabel } from '@utils/helper'
  import { confirm } from '@/decorator'
  import { queryAllocationByItemFormNo, manualAllocate } from '@/api/outbound'
  export default {
    name: 'OutboundAllocation',

    mixins: [pagedSearchMixin, formMixin],

    data() {
      return {
        isCreatedLoadData: false,
        tableName: 'editFormTable',
        pagedUrl: queryStorageAllocate,
        searchFields: [
          {
            label: 'sku编码',
            prop: 'sku_code',
            type: 'label'
          },
          {
            label: '批次',
            prop: 'batch_no'
          },
          {
            label: '质量状态',
            prop: 'material_status',
            type: 'enum',
            props: {
              code: 'MaterialStatus'
            }
          },
          {
            label: '工厂',
            prop: 'factory'
          },
          {
            label: '存放位置',
            prop: 'inventory_area'
          }
        ],
        columns: [
          {
            type: 'selection',
            key: 'selection',
            isView: false,
            attrs: {
              fixed: 'left'
            }
          },
          {
            label: '库位类型',
            key: 'loc_type',
            prop: 'loc_type',
            format(val) {
              return getEnumLabel('StorageType', val)
            },
            width: 100
          },
          {
            label: '库位编号',
            key: 'location_code',
            prop: 'location_code',
            width: 120
          },
          {
            label: '托盘号',
            key: 'container_code',
            prop: 'container_code',
            width: 120
          },
          {
            label: 'sku编码',
            key: 'sku_code',
            prop: 'sku_code',
            width: 160
          },
          {
            label: 'sku名称',
            key: 'sku_name',
            prop: 'sku_name',
            width: 120
          },
          {
            label: '批次',
            key: 'batch_no',
            prop: 'batch_no',
            width: 120
          },
          {
            label: '规格（宽度）',
            key: 'specs_tag',
            prop: 'specs_tag',
            width: 100
          },
          {
            label: '质量状态',
            key: 'material_status',
            prop: 'material_status',
            width: 80
          },
          {
            label: '可用数量',
            key: 'primary_qty',
            prop: 'primary_qty',
            width: 80
          },
          {
            label: '计量单位',
            key: 'primary_unit',
            prop: 'primary_unit',
            width: 100
          },
          {
            label: '批次状态',
            key: 'batch_status',
            prop: 'batch_status',
            format(val) {
              return getEnumLabel('batch_status', val)
            },
            width: 100
          },
          {
            label: '库存状态',
            key: 'inventory_status',
            prop: 'inventory_status',
            format(val) {
              return getEnumLabel('inventory_status', val)
            },
            width: 100
          },
          {
            label: '是否退料',
            key: 'returned_material',
            prop: 'returned_material',
            format(val) {
              return getEnumLabel('YesOrNo', val)
            },
            width: 100
          },
          {
            label: '特殊物料',
            key: 'special_material',
            prop: 'special_material',
            width: 100
          },
          {
            label: '可用性日期',
            key: 'availability_date',
            prop: 'availability_date',
            width: 100
          },
          {
            label: '有效期至',
            key: 'expire_date',
            prop: 'expire_date',
            width: 100
          },
          {
            label: '重检日期',
            key: 're_inspection_date',
            prop: 're_inspection_date',
            width: 100
          },
          {
            label: '生产日期',
            key: 'product_date',
            prop: 'product_date',
            width: 100
          },
          {
            label: '占用数量',
            key: 'locked_qty',
            prop: 'locked_qty',
            width: 100
          }
          // {
          //   label: '辅助计量数量',
          //   key: 'auxiliary_qty',
          //   prop: 'auxiliary_qty',
          //   width: 120
          // },
          // {
          //   label: '辅助计量单位',
          //   key: 'auxiliary_unit',
          //   prop: 'auxiliary_unit',
          //   width: 120
          // },
        ]
      }
    },

    methods: {
      afterEdit() {
        this.selectionKeys = []
        this.handleSearch()
      },

      @confirm('是否确定完成')
      async handleSubmit() {
        await manualAllocate({
          outboundItemDto: this.model,
          storageMaterialIds: this.selectionKeys
        })

        this.handleCancel()
      }
    }
  }
</script>
