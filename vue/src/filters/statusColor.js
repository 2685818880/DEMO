import Vue from 'vue'

Vue.filter('statusColorFilter', (value) => {
  const colorMap = {
    'Created': 'status-green',
    'Executing': 'status-orange',
    'Exception': 'status-red',
    'Finished': 'status'
  }
  return colorMap[status]
})
