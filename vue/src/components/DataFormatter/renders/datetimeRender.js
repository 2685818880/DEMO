export function datetimeRender(h, proxy) {
  const { data, $attrs, formatter } = proxy
  return (
    <span>{formatter.format(data, $attrs['date-format'])}</span>
  )
}
