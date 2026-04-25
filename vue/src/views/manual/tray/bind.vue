<template>
  <resize-layout>
    <resize-col>
      <el-form
        ref="editForm"
        :model="model"
        :rules="rules"
        inline
      >
        <el-form-item prop="houseCode" :label="i18n('仓库')">
          <biz-select
            v-model="model.houseCode"
            bizType="warehouse"
            labelName="house_name"
            valueName="house_no"
          />
        </el-form-item>
        <el-form-item prop="deviceCode" :label="i18n('设备')">
          <device-select
            v-model="model.deviceCode"
          />
        </el-form-item>
        <el-form-item prop="containerCode" :label="i18n('容器号')">
          <el-input v-model="model.containerCode" @keyup.enter.native="handleEnterContainer" />
        </el-form-item>
        <el-form-item prop="serialNo" :label="i18n('物料SN')">
          <el-input ref="snInput" v-model="model.serialNo" @keyup.enter.native="handleEnterSn" />
        </el-form-item>
      </el-form>
      <dynamic-table
        tableName="manualtraybindtable"
        v-loading="loading"
        keyname="serialNo"
        :selectionIds.sync="selectionKeys"
        :columns="columns"
        :data="list"
      >
        <div slot="tools">
          <el-row>
            <el-button
              type="primary"
              :disabled="!hasSelected"
              @click="handleDelete">{{ i18n('删除') }}
            </el-button>
          </el-row>
        </div>
      </dynamic-table>
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
          :disabled="!list.length"
          @click="handleSubmit">
          {{ i18n('组盘完成') }}
        </el-button>
      </el-row>
    </resize-col>
  </resize-layout>
</template>

<script>
import { formMixin, pagedSearchMixin } from '@/mixins'
import { queryMes, savePalletize } from '@/api/palletize'
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

    handleEnterSn() {
      this.searchMaterial()
    },

    async searchMaterial() {
      const { object } = await queryMes({
        serialNo: this.model.serialNo
      })
      if (!object.item) {
        this.$message('没有找到该物料')
        return Promise.reject(new Error('not found sn'))
      }
      this.list.push({
        ...object,
        serialNo: this.model.serialNo
      })
      this.$set(this.model, 'serialNo')
    },

    handleDelete() {
      for (const key of this.selectionKeys) {
        const index = this.list.findIndex(element => element.serialNo === key)
        if (index > -1) {
          this.list.splice(index, 1)
        }
      }
      this.selectionKeys = []
    },

    async submit() {
      const { deviceCode, serialNo, ...etc } = this.model
      await savePalletize({
        ...etc,
        deviceCode: deviceCode[1],
        serialNoList: this.list.map(item => item.serialNo)
      })
    },

    handleCancel() {
      this.resetFields()
      this.list = []
    }
  }
}
</script>
