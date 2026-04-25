<template>
  <el-cascader
    v-bind="$attrs"
    :options="options"
    :show-all-levels="showAllLevels"
    @change="handleChange">
  </el-cascader>
</template>

<script>

export default {
  name: 'LocationTree',

  props: {
    options: {
      type: Array
    },
    showAllLevels: {
      type: Boolean,
      default: false
    }
  },

  methods: {
    handleChange(args) {
      let [storage, location] = args
      this.$emit('update:storage', storage)
      if (!location) {
        location = storage
      }
      this.$emit('update:location', location)
      this.$emit('change', args)
      this.$emit('input', args)
    }
  }
}
</script>
