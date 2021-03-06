
require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name          = package['name']
  s.version       = package["version"]
  s.summary      = "RNAliVodUpload"
  s.description  = package['description']
  s.author        = { 'zhangwilling' => 'zhangwilling0316@gmail.com' }
  s.license       = package['license']
  s.homepage      = package['homepage']
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/zhangwilling/rn-ali-vod-upload.git", :tag => "master" }
  s.source_files  = "RNAliVodUpload/**/*.{h,m}"
  s.requires_arc = true

  s.dependency "React"
  #s.dependency "others"

end

  