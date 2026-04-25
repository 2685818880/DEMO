<template>
  <resize-layout>
    <item-list :createdLoadData="true" />
  </resize-layout>
</template>

<script>
import ItemList from './material/item-list.vue'

export default {
  name: 'RequisitionItem',

  components: { ItemList },

  data() {
    return {}
  }
}
</script>
