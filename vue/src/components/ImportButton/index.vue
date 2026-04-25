<template>
  <div style="display: inline; text-align: left">
    <el-button
      v-bind="$attrs"
      type="primary"
      @click="showForm"
    >
      {{ buttonText }}
    </el-button>
    <upload-form
      ref="uploadForm"
      :action="importAction"
      :templateFileName="templateFileName"
      v-on="$listeners"
    />
  </div>
</template>

<script>
import UploadForm from './upload-form.vue'
import { getStorageMaterialTemplate } from '@/api/inventory'
import { getSkuTemplate } from '@/api/material'

const templateMap = {
  storageMaterial: getStorageMaterialTemplate,
  Sku: getSkuTemplate
}

export default {
  name: 'ImportButton',

  components: { UploadForm },

  props: {
    buttonText: {
      type: String,
      default: '导入'
    },
    importAction: {
      type: String,
      default: ''
    },
    templateType: {
      type: String,
      required: true,
      validator(val) {
        return Object.keys(templateMap).indexOf(val) !== -1
      }
    }
  },

  data() {
    return {
      templateFileName: ''
    }
  },

  methods: {
    async showForm() {
      const templateMethod = templateMap[this.templateType]
      const { object } = await templateMethod()
      this.templateFileName = object.data
      this.$refs.uploadForm.edit()
    }
  }
}
</script>
