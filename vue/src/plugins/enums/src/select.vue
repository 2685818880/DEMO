<!--<template>-->
<!--  <el-select v-if="disable"-->
<!--             v-bind="$attrs"-->
<!--             :value="value"-->
<!--             disabled-->
<!--             @input="updateValue"-->
<!--             :placeholder="placeholder">-->
<!--    <el-option-->
<!--      :label="placeholder"-->
<!--      value=""></el-option>-->
<!--    <el-option-->
<!--      v-for="(enumO, key) in $_MyEnums.enumTypes[type]"-->
<!--      :key="key"-->
<!--      :label="enumO.label"-->
<!--      :value="enumO.value">-->
<!--    </el-option>-->
<!--  </el-select>-->
<!--  <el-select-->
<!--    v-else-->
<!--    v-bind="$attrs"-->
<!--    :value="value"-->
<!--    @input="updateValue"-->
<!--    :placeholder="placeholder">-->
<!--    <el-option-->
<!--      :label="placeholder"-->
<!--      value=""></el-option>-->
<!--    <el-option-->
<!--      v-for="(enumO, key) in $_MyEnums.enumTypes[type]"-->
<!--      :key="key"-->
<!--      :label="enumO.label"-->
<!--      :value="enumO.value">-->
<!--    </el-option>-->
<!--  </el-select>-->
<!--</template>-->

<!--<script>-->

<!--  export default {-->
<!--    name: 'EnumSelect',-->
<!--    props: {-->
<!--      placeholder: {-->
<!--        type: String,-->
<!--        default: '请选择'-->
<!--      },-->
<!--      type: {-->
<!--        required: true-->
<!--      },-->
<!--      value: {-->
<!--        required: true-->
<!--      },-->
<!--      disable: {-->
<!--        type: Boolean,-->
<!--        default: false-->
<!--      }-->
<!--    },-->
<!--    methods: {-->
<!--      updateValue (val) {-->
<!--        this.$emit('input', val)-->
<!--      }-->
<!--    }-->
<!--  }-->
<!--</script>-->
