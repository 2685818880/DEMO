<template>
  <ul class="context-menu-ul">
    <slot>
      <li
        :class="['context-menu-item', { 'is-disabled': option.disabled }]"
        v-for="(option, index) in options"
        :key="index"
        @click="handleMenuItemClick(option)">
        {{ option.label }}
      </li>
    </slot>
  </ul>
</template>

<script>
  export default {
    name: 'ContextMenu',

    props: {
      options: {
        type: Array
      }
    },

    methods: {
      handleMenuItemClick({ command }) {
        this.$emit('change', command)
      }
    }
  }
</script>

<style scoped>
  .context-menu-ul {
    padding: 5px 0;
    margin: 5px 0;
    background-color: #fff;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1)
  }

  .context-menu-item {
    min-width: 100px;
    white-space: nowrap;
    list-style: none;
    line-height: 24px;
    padding: 0 20px;
    margin: 0;
    font-size: 14px;
    color: #606266;
    cursor: pointer;
    outline: none
  }

  .context-menu-item:focus,.context-menu-item:not(.is-disabled):hover {
    background-color: #ecf5ff;
    color: #66b1ff
  }

  .context-menu-item i {
    margin-right: 5px
  }

  .context-menu-item.is-disabled {
    cursor: default;
    color: #bbb;
    pointer-events: none
  }
</style>
