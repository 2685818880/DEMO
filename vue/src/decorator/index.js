function confirm(desc = '是否确定删除信息') {
  return (target, name, description) => {
    const oldValue = description.value
    description.value = function(...rests) {
      return this.$confirm(desc, '提示', {
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then(res => {
        oldValue.apply(this, rests)
      })
    }

    return description
  }
}

function loading(key = 'loading') {
  return (target, name, description) => {
    const oldValue = description.value
    description.value = function(...rests) {
      this[key] = true
      const res = oldValue.apply(this, rests)
      if (res && typeof res.then === 'function') {
        res.finally(() => {
          this[key] = false
        })
      } else {
        this[key] = false
      }
    }
  }
}

export {
  confirm,
  loading
}
