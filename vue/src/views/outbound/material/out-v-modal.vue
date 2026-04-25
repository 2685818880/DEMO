<template>
  <el-dialog :visible.sync="visible" :title="i18n('选择出库')" :modal="false">
    <el-form
      ref="editForm"
      :model="model"
      :rules="rules">
      <el-select v-model="model.location">
        <el-option
         v-for="item in locations"
         :key="item.device_code"
         :label="item.device_name"
         :value="item.device_code"
        ></el-option>
      </el-select>
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
  getDeviceExit
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
    afterEdit() {},
    async getOutLocations() {
      let locationGroup = await getDeviceExit()
      console.log(locationGroup)
      this.locations = locationGroup
    },

    handleChange() {
      this.model = {
        ...this.model,
        outLocation: ''
      }
    },
    handleSubmit() {
      console.log(this.model)
      this.$emit('ok', this.model)
      this.handleCancel()
    }
  }
}
</script>
