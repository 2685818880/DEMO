<template>
  <el-dialog :visible.sync="visible" :title="i18n('选择出库')" :modal="false">
    <el-form
      ref="editForm"
      :model="model"
      :rules="rules">
      <el-form-item :label="i18n('请选择取出口')" prop="quality_status">
        <el-select
          filterable
          v-model="model.outLocation"
          :placeholder="i18n('请选择取出口')">
          <el-option
            v-for="item in outLocations"
            :key="item.code"
            :label="item.value"
            :value="item.code">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <div slot="footer">
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading">{{i18n('确定')}}
      </el-button>
      <el-button @click="handleCancel">{{i18n('取消')}}</el-button>
    </div>
  </el-dialog>
</template>

<script>
  import { formMixin } from '@/mixins'
  import {
    getStayWireOutLocations,
     takeOutOutbound,
     takeOutOutboundItem,
     takeOutOutboundAllocation
     } from '@/api/outbound'

  export default {
    name: 'OutboundOutModal',

    mixins: [formMixin],

    props: {
      level: {
        required: true
      }
    },

    data() {
      return {
        rules: {
          outLocation: [{ required: true, message: i18n('请选择出库口') }]
        },
        outLocations: []
      }
    },

    async created() {
      this.outLocations = await getStayWireOutLocations()
    },

    methods: {
      async submit() {
        const param = {}
        param.outLocation = this.model.outLocation

        if (this.level === 'outbound') {
          param.outboundIds = this.model.selectionKeys
          await takeOutOutbound(param)
        } else if (this.level === 'outboundItem') {
          param.outboundItemIds = this.model.selectionKeys
          await takeOutOutboundItem(param)
        } else if (this.level === 'outboundAllocation') {
          param.outboundAllocationIds = this.model.selectionKeys
          await takeOutOutboundAllocation(param)
        }
      }
    }
  }
</script>
