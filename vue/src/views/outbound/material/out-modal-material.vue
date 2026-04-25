<template>
  <drawer-modal
    :visible.sync="visible"
    :title="i18n('选择出库')"
    :modal="false"
    :loading="loading"
    @ok="handleSubmit"
    @close="handleCancel"
  >
    <el-form
      ref="editForm"
      :model="model"
      :rules="rules">
      <el-form-item :label="i18n('请选择取出口')" prop="outLocation">
        <el-select
          filterable
          v-model="model.outLocation"
          :placeholder="i18n('请选择出库口')">
          <el-option
            v-for="item in outLocations"
            :key="item.device_code"
            :label="item.device_name"
            :value="item.device_code">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
  </drawer-modal>
</template>

<script>
import {formMixin} from '@/mixins'
import {
  getMaterialOutLocations,
  getLocations,
  takeOutOutbound,
  takeOutOutboundItem,
  takeOutOutboundAllocation,
  getDeviceExit
} from '@/api/outbound'
import BizSelect from '@/components/BizSelect'

export default {
  name: 'OutboundOutModal',

  components: { BizSelect },

  mixins: [formMixin],

  props: {
    level: {
      required: true
    }
  },

  data() {
    return {
      rules: {
        outLocation: [{required: true, message: this.i18n('请选择出库口')}]
      },
      outLocations: []
    }
  },

  methods: {
    async afterEdit() {
      if (this.level === 'outbound') {
        this.outLocations = await getDeviceExit({
          house_code: this.model.house_code
        })
      }
      if (this.level === 'outboundAllocation') {
        this.outLocations = await getDeviceExit({
          house_code: this.model.house_code
        })
      }
    },

    async submit() {
      const param = {}
      param.toPos = this.model.outLocation

      if (this.level === 'outbound') {
        param.outboundIds = this.model.selectionKeys
        await takeOutOutbound(param)
      } else if (this.level === 'outboundItem') {
        param.outboundItemIds = this.model.selectionKeys
        await takeOutOutboundItem(param)
      } else if (this.level === 'outboundAllocation') {
        param.outboundIds = this.model.selectionKeys
        await takeOutOutboundAllocation(param)
      }
    }
  }
}
</script>
