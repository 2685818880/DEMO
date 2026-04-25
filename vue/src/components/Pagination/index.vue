<template>
  <el-pagination
    :page-sizes="pageSizes"
    v-bind="$attrs"
    v-on="$listeners"
    background
    :layout="layout">
  </el-pagination>
</template>

<script>
  export default {
    name: 'Pagination',

    props: {
      pageSizes: {
        type: Array,
        default() {
          return [10, 20, 30]
        }
      },
      layout: {
        type: String,
        default: 'total, sizes, prev, pager, next, jumper'
      }
    }
  }
</script>
