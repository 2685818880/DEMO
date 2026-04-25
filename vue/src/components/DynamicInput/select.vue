<template>
  <el-select
    :value="value"
    @input="handleInput"
    v-bind="$attrs">
    <el-option
      v-for="option in options"
      :key="option[selectValue]"
      :label="option[selectLabel]"
      :value="option[selectValue]">
    </el-option>
  </el-select>
</template>

<script>
  import fetch from '@/utils/fetch.js'

  export default {
    name: 'DynamicSelect',
    props: {
      value: {
        required: true
      },
      remoteUrl: {
        type: String
      },
      selectLabel: {},
      selectValue: {}
    },
    data() {
      return {
        options: []
      }
    },
    created() {
      fetch(this.remoteUrl).then(res => {
        this.options = res
      })
    },
    methods: {
      handleInput(val) {
        this.$emit('input', val)
      }
    }
  }
</script>
