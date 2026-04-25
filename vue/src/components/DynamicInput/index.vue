<script>
  import DynamicSelect from './select'

  export default {
    name: 'DynamicInput',
    props: {
      componentName: {
        type: String,
        required: true
      },
      value: {
        required: true
      },
      attrs: {
        required: true
      }
    },
    components: {
      DynamicSelect
    },
    render(h) {
      return h(this.componentName, {
        attrs: {
          value: this.value,
          ...this.attrs,
          ...this.$attrs
        },
        on: {
          ...this.$listeners
        }
      }, [this.$slots.default])
    }
  }
</script>
