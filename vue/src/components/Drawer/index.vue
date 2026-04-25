<template>
  <el-drawer
    class="g-drawerModal"
    :title="title"
    :size="size"
    :visible="visible"
    direction="rtl"
    :wrapperClosable="wrapperClosable"
    v-bind="$attrs"
    @closed="handleCancel"
    :before-close="handleCancel">
    <div class="g-drawer-content">
      <slot></slot>
    </div>
    <div class="g-drawer-footer">
      <slot name="footer"></slot>
    </div>
  </el-drawer>
</template>

<script>
  export default {
    name: 'DrawerModal',

    props: {
      visible: {
        type: Boolean,
        default: false
      },
      size: {
        default: '95%'
      },
      title: {
        default: ''
      },
      wrapperClosable: {
        default: false
      }
    },

    methods: {
      handleCancel() {
        this.$emit('update:visible', false)
      }
    }
  }
</script>

<style scoped>

</style>
