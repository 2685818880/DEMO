/**
 * 弹出默认混入项
 */
export default {
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      closed: this.visible,
      loading: false
    }
  },
  watch: {
    visible(val) {
      this.closed = val
    },
    closed(val) {
      this.$emit('update:visible', val)
    }
  },
  methods: {
    handleCancel() {
      this.closed = false
    }
  }
}
