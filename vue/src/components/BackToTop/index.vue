<template>
  <div
    class="back"
    v-if="visible"
    @click="backToTop">
    <svg-icon
      iconClass="back-to-top"
      className="backIcon">
    </svg-icon>
  </div>
</template>

<script>
  import _ from 'lodash'

  export default {
    name: 'backToTop',
    props: {
      visibleHeight: {
        type: Number,
        default: 400
      },
      backPosition: {
        type: Number,
        default: 0
      }
    },
    data() {
      return {
        visible: false
      }
    },
    created() {
      this.throttleScroll = _.throttle(_.throttle(this.handleScroll, 17))
      window.addEventListener('scroll', this.throttleScroll)
    },

    methods: {
      handleScroll() {
        if (window.scrollY > this.visibleHeight) {
          this.visible = true
        } else {
          this.visible = false
        }
      },
      backToTop() {
        window.scrollTo(0, this.backPosition)
      }
    },
    beforeDestroy() {
      window.removeEventListener('scroll', this.throttleScroll)
    }
  }
</script>

<style scoped>
  .back {
    position: fixed;
    bottom: 30px;
    right: 30px;
  }
  .backIcon {
    fill: #9aaabf;
    font-size: 24px;
  }
</style>
