<template>
  <el-select
    v-bind="$attrs"
    v-on="$listeners"
  >
    <el-option value="">{{i18n('请选择')}}</el-option>
    <el-option
      v-for="option in options"
      :key="option[valueName]"
      :value="option[valueName]"
      :label="option[labelName]"
    >
      <span v-html="option[labelName]"></span>
    </el-option>
  </el-select>
</template>

<script>
  import { queryDevice, queryAllHouse } from '@/api/warehouse'
  import { getLocationsByHouse } from '@/api/outbound'
  import { getContainerType } from '@/api/palletize/index'
  import { getStoreHouse } from '@/api/storage-location'
  import { getSkuCategory } from '@/api/material'

  // 业务类型
  export const BizTypes = {
    warehouse: 'warehouse',
    locations: 'locations',
    skuCategory: 'skuCategory',
    containerType: 'containerType',
    store: 'store',
    exit: 'exit'
  }

    async function getExit() {
    const { rows } = await queryDevice({
      device_type: 'Exit',
      page: 1,
      row: 1000
    })
    return {
      rows
    }
  }

  // 业务请求方法
  async function getLocations() {
    const houseCode = this.$attrs.houseCode
    const locationType = this.$attrs.locationType || 'Entrance'
    let rows = []
    houseCode && (rows = await getLocationsByHouse(houseCode, locationType))
    return {
      rows
    }
  }
  async function getSkuCategoryList() {
    const { rows } = await getSkuCategory()
    rows.forEach(item => {
      if (item.level > 1) {
        item.category_name = `${item.category_name}`
      }
    })
    return {
      rows
    }
  }
  const BizTypeMethods = {
    warehouse: queryAllHouse,
    locations: getLocations,
    skuCategory: getSkuCategoryList,
    containerType: getContainerType,
    store: getStoreHouse,
    exit: getExit
  }

  export default {
    name: 'BizSelect',

    props: {
      bizType: {
        required: true,
        validator(value) {
          return Object.values(BizTypes).indexOf(value) > -1
        }
      },
      valueName: {
        default: 'id'
      },
      labelName: {
        default: 'name'
      }
    },

    data() {
      return {
        options: []
      }
    },

    computed: {
      bizMethod() {
        return BizTypeMethods[this.bizType]
      }
    },

    async created() {
      const data = await this.bizMethod()
      this.options = data.rows || data
      this.$emit('init', this.options)
    }
  }
</script>
