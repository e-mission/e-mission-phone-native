//
//  UIImage+EMS.m
//  E-Mission
//
//  Created by Neeraj Baid on 2/5/15.
//  Copyright (c) 2015 Kalyanaraman Shankari. All rights reserved.
//

#import "UIImage+EMS.h"

@implementation UIImage (EMS)

+ (UIImage *)colorizeImage:(UIImage *)image withColor:(UIColor *)color
{
    CGRect rect = CGRectMake(0, 0, image.size.width, image.size.height);
    
    UIGraphicsBeginImageContext(rect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextClipToMask(context, rect, image.CGImage);
    CGContextSetFillColorWithColor(context, [color CGColor]);
    CGContextFillRect(context, rect);
    image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    UIImage *flippedImage = [UIImage imageWithCGImage:image.CGImage
                                                scale:1.0 orientation: UIImageOrientationDownMirrored];
    return flippedImage;
}

@end
