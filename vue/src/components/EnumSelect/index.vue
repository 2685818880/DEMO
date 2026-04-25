<template>
  <el-select
    :value="value"
    v-bind="$attrs"
    v-on="$listeners"
    @change="handleChange">
    <el-option label="请选择" value=""></el-option>
    <el-option
      v-for="item in options"
      :key="item.value"
      :label="item.label"
      :value="item.value"/>
  </el-select>
</template>

<script>
  import EnumsOptions from '@/utils/enums-options'

  export default {
    name: 'EnumSelect',

    props: {
      value: {
        default: ''
      },
      code: {
        type: String,
        required: true
      }
    },

    computed: {
      options() {
        return EnumsOptions[this.code] || []
      }
    },

    methods: {
      handleChange(val) {
        this.$emit('input', val)
      }
    }
  }
</script>

<style scoped>

</style>
