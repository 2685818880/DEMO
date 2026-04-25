<template>
  <span>{{ label }}</span>
</template>

<script>
import queryMixin from '@comp/queryMixin'

export default {
  name: 'BizLabel',

  props: {
    // 对应值
    value: {
      required: true
    },
    // 值对
    fieldName: {
      required: true
    },
    showFieldName: {
      required: true
    }
  },

  mixins: [queryMixin],

  async created() {
    const {rows} = await this.getData()
    this.list = rows
  },

  computed: {
    label() {
      const curData = this.list.find(item => item[this.fieldName] === this.value)
      if (!curData) {
        return ''
      }
      return curData[this.showFieldName] || ''
    }
  }

}

</script>
