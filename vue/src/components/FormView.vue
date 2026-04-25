<template>
  <el-form
    class="form-view">
    <el-row :gutter="gutter">
      <el-col v-for="({ label, prop, format }) in showFields" :key="prop" :lg="lgSpan" :sm="span" :xl="xlSpan">
        <el-form-item :label="i18n(label)+':'">
          <template v-if="format">
            {{ format(fieldsValue[prop]) }}
          </template>
          <template v-else>
            {{ fieldsValue[prop] }}
          </template>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
  export default {
    props: {
      fieldsValue: {
        type: Object
      },

      fieldsDesc: {
        type: Array
      },

      cols: {
        type: Number,
        default: 2
      },

      gutter: {
        type: Number,
        default: 10
      }
    },

    computed: {
      showFields() {
        return this.fieldsDesc.filter(item => item.isView !== false)
      },
      span() {
        return 24 / this.cols
      },
      lgSpan() {
        return 24 / (this.cols + 1)
      },
      xlSpan() {
        return 24 / (this.cols + 2)
      },
      bodyStyle() {
        return this.visible ? { padding: '20px' } : {}
      }
    }
  }
</script>
