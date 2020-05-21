#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'yandex_checkout'
  s.version          = '0.0.1'
  s.summary          = 'A Yandex Checkout plugin for Flutter.'
  s.description      = <<-DESC
A Yandex Payment plugin for Flutter.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.vendored_frameworks = 'Frameworks/TMXProfiling.framework', 'Frameworks/TMXProfilingConnections.framework'
  s.ios.deployment_target = '8.0'
end

