<template>
  <span :class="label">
    <slot :cell="$_MyEnums.enumTypes[type][label] || {}">
      {{ $EnumTranslate(type, label)}}
    </slot>
  </span>
</template>

<script>

  export default {
    name: 'EnumLabel',
    props: {
      type: {
        required: true
      },
      label: {
        required: true
      }
    }
  }
</script>
