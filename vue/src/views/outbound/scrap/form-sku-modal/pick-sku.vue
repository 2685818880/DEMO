<script>
import { findAvailableMaterial } from '@/api/outbound'
import { getEnumLabel } from '@/utils/helper'
import PickSku from '../../material/form-sku-modal/pick-sku.vue'

export default {
  name: 'SkuModal',

  extends: PickSku,

  data() {
    return {
      keyname: 'serial_no',
      tableName: 'scrapSkuTable',
      pagedUrl: findAvailableMaterial,
      searchFields: [
        {
          label: 'SKU编号',
          prop: 'sku_code'
        },
        {
          label: 'SKU名称',
          prop: 'sku_name'
        },
        {
          label: '工厂',
          prop: 'factory_code'
        },
        {
          label: '库存地点',
          prop: 'inventory_location'
        }
      ],
      columns: [
        {
          type: 'selection',
          key: 'selection',
          attrs: {
            fixed: 'left',
            selectable: (row, index) => {
              const selectedRows = this.selectedRows
              if (selectedRows.filter(item => item.serial_no === row.serial_no).length) {
                return false
              }
              return true
            }
          }
        },
        {
          label: '库区',
          key: 'house_code',
          prop: 'house_code'
        },
        {
          label: 'SN',
          key: 'serial_no',
          prop: 'serial_no',
          width: 220
        },
        {
          label: '库位号',
          key: 'location_code',
          prop: 'location_code',
          width: 120
        },
        {
          label: '库位类型',
          key: 'loc_type',
          prop: 'loc_type',
          format(val) {
            return getEnumLabel('StorageType', val)
          }
        },
        {
          label: '托盘编号',
          key: 'container_code',
          prop: 'container_code',
          width: 120
        },
        {
          label: '存货编码',
          key: 'category_code',
          prop: 'category_code',
          width: 120
        },
        {
          label: '存货名称',
          key: 'category_name',
          prop: 'category_name',
          width: 120
        },
        {
          label: '物料编码',
          key: 'sku_code',
          prop: 'sku_code',
          width: 120
        },
        {
          label: '物料名称',
          key: 'sku_name',
          prop: 'sku_name',
          width: 120
        },
        {
          label: '库存数量',
          key: 'primary_qty',
          prop: 'primary_qty'
        },
        {
          label: '库存单位',
          key: 'primary_unit',
          prop: 'primary_unit'
        },
        {
          label: '辅助数量',
          key: 'auxiliary_qty',
          prop: 'auxiliary_qty'
        },
        {
          label: '辅助计量单位',
          key: 'auxiliary_unit',
          prop: 'auxiliary_unit'
        },
        {
          label: '库存可用数量',
          key: 'available_qty',
          prop: 'available_qty'
        },
        {
          label: '质量状态',
          key: 'material_status',
          prop: 'material_status',
          format(val) {
            return getEnumLabel('MaterialStatus', val)
          }
        },
        {
          label: '批次状态',
          key: 'batch_status',
          prop: 'batch_status',
          format(val) {
            return getEnumLabel('BatchStatus', val)
          }
        },
        {
          label: '库存状态',
          key: 'inventory_status',
          prop: 'inventory_status',
          format(val) {
            return getEnumLabel('InventoryStatus', val)
          }
        }
      ]
    }
  },

  methods: {
    handleSubmit() {
      try {
        const skuArr = this.selectionRows
        // if (skuArr.filter(item => !item.num).length) {
        //   this.$message('请输入需求数量')
        //   return false
        // }
        this.$emit('ok', skuArr)
        this.handleCancel()
      } catch (e) {
        console.log(e)
      }
    },

    afterEdit() {
      this.handleSearch()
    }
  }
}
</script>
