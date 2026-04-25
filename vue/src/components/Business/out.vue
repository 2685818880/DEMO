<template>
  <el-select
    filterable
    v-bind="$attrs"
    v-on="$listeners"
    clearable>
    <el-option
      v-for="item in entries"
      :key="item.key"
      :label="item.value"
      :value="item.key">
    </el-option>
  </el-select>
</template>

<script>
import {
  getEntrance,
} from '@/api/palletize/index'
export default {
  name: 'BizOutSelect',

  data() {
    return {
      entries: {}
    }
  },

  created() {
    this.getEntrySite()
  },

  methods: {
    async getEntrySite() {
      const entries = await getEntrance()
      this.entries = entries
    }
  }
}
</script>
