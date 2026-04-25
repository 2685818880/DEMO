<template>
  <el-input
    v-bind="$attrs"
    @input="handleChange"
    clearable />
</template>

<script>
export default {
  name: 'BizContainer',

  methods: {
    handleChange(val) {
      this.$emit('input', val.toUpperCase())
    }
  }
}
</script>
