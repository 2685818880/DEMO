<script>
  import Vue from 'vue'

  export default {
    name: 'dynamicForm',
    data() {
      return {
        formData: {}
      }
    },
    props: {
      model: {
        type: Object,
        required: true
      },
      items: {
        type: Array,
        required: true
      }
    },
    render(h) {
      return (
        <el-form
      model = {this.formData}
      {...{attrs: this.$attrs}}>
      {
        this.items.map(item => {
          let scopedSlot
          // if (item.scopedSlot) {
          //   scopedSlot = this.$scopedSlots[item.scopedSlot] &&
          //                   this.$scopedSlots[item.scopedSlot]({item, form: this.formData})
          // } else {
          let slotShapes = item.slotShapes
          Array.isArray(slotShapes) || (slotShapes = [slotShapes])

          scopedSlot = slotShapes.map(slotShape => {
            let name = slotShape.componentName
            if (!name) throw new Error('componentName必须给')
            const componentName = `${name.charAt(0).toUpperCase()}${name.slice(1)}`

            if (typeof Vue.component(componentName) !== 'function') {
              throw new Error('给定的组件名不存在')
            }
            return h(componentName, {
              props: {
                value: this.formData[slotShape.value],
                ...slotShape.attrs
              },
              on: {
                input: (value) => {
                  this.formData[slotShape.value] = value
                  this.$emit('input', {...this.formData})
                },
                ...slotShape.on
              }
            }, this.$slots[slotShape.slot])
          })
          // }
          return (
            <el-form-item {...{attrs: {...item.attrs}}}>
          {scopedSlot}
        </el-form-item>
        )
        })
      }
    </el-form>
    )
    },
    methods: {
      renderItem(name) {
      },
      label(name) {
        return () => (
          <label>`未找到对应的${name}`</label>
      )
      }
    },
    watch: {
      model: {
        handler(val) {
          this.formData = {...val}
        },
        immediate: true
      }
    }
  }
</script>
