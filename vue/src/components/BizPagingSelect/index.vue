<template>
  <paging-select
    v-bind="$attrs"
    v-on="$listeners"
    :search="search"
  />
</template>

<script>
import { getCustomer, getSupplier } from '@/api/warehouse'

// 业务类型
export const BizTypes = {
  customer: {
    keywordsName: 'customer_name',
    api: getCustomer
  },
  supplier: {
    keywordsName: 'supplier_name',
    api: getSupplier
  }
}

export default {
  name: 'BizPagingSelect',

  props: {
    bizType: {
      required: true,
      validator(value) {
        return Object.keys(BizTypes).indexOf(value) > -1
      }
    }
  },

  methods: {
    async search({ keywords, page, limit }) {
      const { keywordsName, api } = BizTypes[this.bizType]
      const params = {
        ...this.$attrs['extra-params'],
        page,
        row: limit
      }
      params[keywordsName] = keywords
      const { rows, total } = await api(params)
      console.log({
        data: rows,
        total
      })
      return {
        data: rows,
        total
      }
    }
  }
}
</script>
