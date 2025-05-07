# Carousel Images

This directory contains images for the carousel in the Home page.

## Required Images

The following images are required for the carousel to work properly:

1. `carousel1.jpg` - Main carousel image
2. `carousel2.jpg` - Second carousel image
3. `carousel3.jpg` - Third carousel image

## Image Specifications

For best results, use images with the following specifications:

- Resolution: 1200x400 pixels
- Format: JPG or PNG
- File size: Optimize for web (under 500KB each)

## Adding Your Own Images

To add your own images to the carousel:

1. Replace the existing image files with your own images
2. Make sure to keep the same filenames (`carousel1.jpg`, `carousel2.jpg`, `carousel3.jpg`)
3. If you want to use different filenames, update the `loadImages()` method in `HomeController.java`

## Troubleshooting

If the carousel images don't appear:

1. Check that the image files exist in this directory
2. Verify that the filenames match exactly (case-sensitive)
3. Ensure the images are in a supported format (JPG or PNG) 