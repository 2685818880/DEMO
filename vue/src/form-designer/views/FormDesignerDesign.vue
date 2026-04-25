<template>
  <div class="form-designer-design">
    <FormDesignerHome />
  </div>
</template>

<script>
import FormDesignerHome from '@/form-designer/views/index/Home'

export default {
  name: 'FormDesignerDesign',
  components: { FormDesignerHome },
  created() {
    const id = this.$route.params.id
    if (id) {
      // TODO: 根据ID加载已有表单配置
      console.log('加载表单配置:', id)
    }
  }
}
</script>

<style lang="scss" scoped>
.form-designer-design {
  width: 100%;
  height: 100%;
}
</style>
