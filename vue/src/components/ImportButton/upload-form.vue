<template>
  <drawer-modal
    :title="i18n('导入')"
    :visible.sync="visible"
    :loading="loading"
    @ok="handleSubmit"
    @close="handleCancel"
  >
    <el-form :model="form">
      <el-form-item>
        <el-upload
          ref="upload"
          :auto-upload="false"
          :data="form"
          v-bind="$attrs"
          :multiple="false"
          :limit="1"
          accept=".xls,.xlsx"
          :on-success="onSuccess"
        >
          <el-row>
            <el-button size="small" type="primary">{{i18n('点击上传')}}</el-button>
            <el-button @click.stop="downloadTemplate">{{i18n('下载模板')}}</el-button>
          </el-row>
          <div class="el-upload__tip" slot="tip">
            {{i18n('只能上传xls/xlsx文件')}}
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item prop="updateSupport">
        <el-checkbox v-model="form.updateSupport">{{i18n('是否覆盖已存在的数据')}}</el-checkbox>
      </el-form-item>
    </el-form>
  </drawer-modal>
</template>

<script>
import { formMixin } from '@/mixins'
import { loading } from '@/decorator'

const downloadFilePath = `${process.env.API_BASE_URL}/common/download`

export default {
  name: 'UploadForm',

  mixins: [formMixin],

  props: {
    templateFileName: {
      type: String,
      default: ''
    }
  },

  data() {
    return {
      visible: false,
      form: {
        updateSupport: false
      },
      loading: false
    }
  },

  methods: {
    async downloadTemplate() {
      location.href = `${downloadFilePath}?fileName=${this.templateFileName}`
    },

    handleCancel() {
      this.$refs.upload.clearFiles()
      this.form.updateSupport = false
      this.visible = false
      this.loading = false
    },

    @loading()
    async handleSubmit() {
      this.loading = true
      await this.$refs.upload.submit()
    },

    onSuccess() {
      this.$emit('ok')
      this.$message({
        message: '导入成功',
        type: 'success'
      })
      this.handleCancel()
    }
  }
}
</script>

<style>
.download-row {
  display: flex;
}
.el-upload__input {
  display: none!important;;
}
</style>
