<template>
  <drawer-modal
    :visible.sync="visible"
    :title="i18n('需求单详情')"
    width="80%"
  >
    <resize-layout :height="modalTableHeight">
      <item-list ref="itemList" :model="model" />
    </resize-layout>
    <div slot="footer">
      <el-button @click="handleCancel">{{$t('取消')}}</el-button>
    </div>
  </drawer-modal>
</template>

<script>
import {formMixin} from '@/mixins'
import ItemList from './item-list.vue'

export default {
  name: 'OutboundMaterielList',

  mixins: [formMixin],

  components: {
    ItemList
  },

  data() {
    const {formType: form_type} = this.$route.params

    return {
    }
  },

  methods: {
    afterEdit() {
      this.$refs.itemList.afterEdit()
    }
  }
}
</script>
