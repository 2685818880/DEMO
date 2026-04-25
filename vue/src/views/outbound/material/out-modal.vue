<template>
  <el-dialog :visible.sync="visible" :title="i18n('选择出库')" :modal="false">
    <el-form
      ref="editForm"
      :model="model"
      :rules="rules">
      <location-tree
        v-model="areas"
        :storage.sync="model.currentStorage"
        :location.sync="model.outLocation"
        :options="locations"
        :props="{ checkStrictly: true }">
      </location-tree>
    </el-form>

    <div slot="footer">
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading">{{ i18n('确定') }}
      </el-button>
      <el-button @click="handleCancel">{{ i18n('取消') }}</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {formMixin} from '@/mixins'
import {
  getOutLocations,
  takeOutOutbound,
  takeOutOutboundItem,
  takeOutOutboundAllocation
} from '@/api/outbound'
import LocationTree from '@comp/LocationTree/index'

export default {
  name: 'OutboundOutModal',

  mixins: [formMixin],

  components: {
    LocationTree
  },

  props: {
    level: {
      required: true
    }
  },

  data() {
    return {
      rules: {
        currentStorage: [{ required: true, message: this.i18n('请选择库区'), trigger: 'change' }]
      },
      locationGroups: {},
      locations: []
    }
  },

  created() {
    this.getOutLocations()
  },

  computed: {
    areas() {
      return [this.model.currentStorage, this.model.outLocation]
    }
  },

  methods: {
    afterEdit() {
      console.log(this.model.currentStorage, this.model.outLocation)
    },
    async getOutLocations() {
      const locationGroup = await getOutLocations()
      this.locations = locationGroup
    },

    handleChange() {
      this.model = {
        ...this.model,
        outLocation: ''
      }
    },
    async submit() {
      const param = {}

      if (this.model.outLocation) {
        param.outLocation = this.model.outLocation
      } else {
        param.outLocation = this.model.currentStorage
      }

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
