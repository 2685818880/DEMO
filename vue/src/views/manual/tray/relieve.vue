<template>
  <resize-layout>
    <resize-col>
      <el-form
        ref="editForm"
        :model="model"
        :rules="rules"
      >
        <el-form-item prop="house_code" :label="i18n('仓库')">
          <biz-select
            v-model="model.house_code"
            bizType="warehouse"
            labelName="house_name"
            valueName="house_no"
          />
        </el-form-item>
        <el-form-item prop="container_code" :label="i18n('容器号')">
          <el-input v-model="model.container_code" @keyup.enter.native="handleEnterContainer" style="width: 250px" />
        </el-form-item>
      </el-form>
      <!-- <dynamic-table
        tableName="manualtraybindtable"
        v-loading="loading"
        keyname="serialNo"
        :selectionIds.sync="selectionKeys"
        :columns="columns"
        :data="list"
      >
      </dynamic-table> -->
      <el-row
        class="mt10"
        :gutter="20"
        type="flex"
        justify="space-around">
        <el-button
          size="middle"
          type="primary"
          round
          :loading="loading"
          @click="handleSubmit">
          {{ i18n('整盘拆除') }}
        </el-button>
      </el-row>
    </resize-col>
  </resize-layout>
</template>

<script>
import { formMixin, pagedSearchMixin } from '@/mixins'
import { queryMes, savePalletize, relievePalletize } from '@/api/palletize'
import DeviceSelect from '@/components/device-select'
import BizSelect from '@/components/BizSelect'

export default {
  name: 'Traybind',

  components: { DeviceSelect, BizSelect },

  mixins: [formMixin, pagedSearchMixin],

  data() {
    return {
      isCreatedLoadData: false,
      rules: {
        house_code: [
          { required: true, message: '请选择仓库', trigger: 'change' }
        ],
        container_code: [
          { required: true, message: '请输入托盘号', trigger: 'blur' }
        ]
      },
      columns: [
        {
          type: 'selection',
          key: 'selection',
          attrs: {
            fixed: 'left'
          }
        },
        {
          label: '物料sn',
          prop: 'serialNo',
          key: 'serialNo',
          width: 180
        },
        {
          label: 'sku编码',
          prop: 'item',
          key: 'item'
        },
        {
          label: '每卷料的片数',
          prop: 'qty',
          key: 'qty',
          width: 120
        },
        {
          label: '接收时间',
          prop: 'receiveDateTime',
          key: 'receiveDateTime',
          width: 160
        },
        {
          label: '区域',
          prop: 'location',
          key: 'location'
        },
        {
          label: '工单号',
          prop: 'shopOrder',
          key: 'shopOrder',
          width: 120
        },
        {
          label: '存货分类编码',
          prop: 'productionCode',
          key: 'productionCode',
          width: 120
        }
      ],
      list: []
    }
  },

  methods: {
    handleEnterContainer() {
      this.$refs.snInput.focus()
    },

    async submit() {
      await relievePalletize({
        ...this.model,
        is_all: true
      })
    },

    handleCancel() {
      this.resetFields()
      this.list = []
    }
  }
}
</script>
