<script>
  import { formatMap } from './mapper'
  export default {
    name: 'DataFormatter',

    props: {
      data: {
        default: '',
        validator(value) {
          return typeof value === 'object' || typeof value === 'string' || typeof value === 'number'
        }
      },

      formatType: {
        type: String,
        default: 'default'
      },

      render: {
        type: Function,
        validator(value) {
          return typeof value === 'function'
        }
      }
    },

    computed: {
      formatter() {
        const { format, render } = formatMap[this.formatType] || formatMap.default
        return {
          data: format(this.data),
          render,
          format
        }
      }
    },

    render(h) {
      return (
        <span>
          {this.render ? this.render(h) : this.formatter.render(h, this)}
          <slot oldData={this.data} newData={this.formatter.data} />
        </span>
      )
    }
  }
</script>
