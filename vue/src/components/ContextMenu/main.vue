<script>
  import ContextMenu from './menu'

  export default {
    name: 'ContextMenuPopover',

    components: {
      ContextMenu
    },

    data() {
      return {
        visible: false,
        position: {
          top: 0,
          left: 0
        },
        content: '',
        options: []
      }
    },

    computed: {
      style() {
        return {
          top: `${this.position.top}px`,
          left: `${this.position.left}px`
        }
      }
    },

    render(h) {
      let { visible, style, $slots: { default: defaultContent, menu }, content } = this
      if (!visible) {
        return null
      }

      if (typeof content === 'function') {
        content = content(h)
      }

      return (
        <div
          ref="contextmenu"
          class="context-menu"
          style={style}>
          { defaultContent }
          { menu }
          { content }
        </div>
      )
    },

    mounted() {
      document.body.addEventListener('click', this.handleBodyClick)
      document.body.addEventListener('contextmenu', this.handleBodyClick)
    },

    beforeDestroy() {
      document.body.removeEventListener('click', this.handleBodyClick)
      document.body.removeEventListener('contextmenu', this.handleBodyClick)
    },

    methods: {
      handleBodyClick() {
        this.hide()
      },

      show({ position }) {
        if (position) {
          this.position = position
        }

        this.visible = true

        this.$nextTick(() => {
          this.handleAutoPlacement()
        })
      },

      setContent(content) {
        this.content = content
      },

      handleAutoPlacement() {
        let { top, left } = this.position

        const { clientWidth: contextmenuWidth, clientHeight: contextmenuHeight } = this.$refs.contextmenu
        if (contextmenuHeight + top >= window.innerHeight) {
          top -= contextmenuHeight
        }
        if (contextmenuWidth + left >= window.innerWidth) {
          left -= contextmenuWidth
        }

        this.position = {
          top,
          left
        }
      },

      hide() {
        this.visible = false
      }
    }
  }
</script>

<style scoped>
  .context-menu {
    position: absolute;
    z-index: 2100
  }
</style>
