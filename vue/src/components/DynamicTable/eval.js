function treeToData(data, expandAll, parent = null, level = null) {
  let result = []
  data.forEach(record => {
    record = {...record, _expand: expandAll}
    let _level = 1
    if (Number.isInteger(level)) {
      _level = level + 1
    }
    record = {...record, _level}

    if (parent) {
      record = {...record, parent, pid: parent.id}
    }
    record = {...record, _checked: record.checked}
    result.push(record)
    if (record.children) {
      const children = treeToData(record.children, expandAll, record, _level)
      result = result.concat(children)
    }
  })
  return result
}

export default treeToData
